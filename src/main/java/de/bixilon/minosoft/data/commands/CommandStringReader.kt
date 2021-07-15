/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.commands

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.stream.JsonReader
import de.bixilon.minosoft.data.commands.parser.exceptions.BooleanCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.InvalidJSONCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.StringCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.nbt.CompoundTagBadFormatCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.nbt.ListTagBadFormatCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.number.DoubleCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.number.FloatCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.number.IntegerCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.number.LongCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.properties.BadPropertyMapCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.properties.DuplicatedPropertyKeyCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.resourcelocation.InvalidResourceLocationCommandParseException
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.util.Pair
import de.bixilon.minosoft.util.Util
import java.io.StringReader
import java.util.*
import java.util.regex.Pattern

open class CommandStringReader {
    val string: String
    var cursor = 0

    constructor(string: String) {
        this.string = string
    }

    constructor(stringReader: CommandStringReader) {
        string = stringReader.string
        cursor = stringReader.cursor
    }

    val remainingLength: Int
        get() = string.length - cursor

    val totalLength: Int
        get() = string.length

    fun getRead(): String {
        return string.substring(0, cursor)
    }

    val remaining: String
        get() = string.substring(cursor)

    @JvmOverloads
    fun canRead(length: Int = 1): Boolean {
        return string.length - (cursor + length) >= 0
    }

    fun peek(): Char {
        return string[cursor]
    }

    fun read(): Char {
        return string[cursor++]
    }

    @JvmOverloads
    fun skip(length: Int = 1) {
        val nextLength = cursor + length
        check(!(nextLength > string.length || nextLength < 0)) { "Nothing to skip!" }
        cursor = nextLength
    }

    /**
     * @return The number of skipped whitespaces
     */
    fun skipWhitespaces(): Int {
        var skipped = 0
        while (canRead() && Character.isWhitespace(peek())) {
            skip()
            skipped++
        }
        return skipped
    }

    fun readUnquotedString(): String {
        val builder = StringBuilder()
        while (canRead()) {
            val next = peek()
            if (next in '0'..'9' || next in 'A'..'Z' || next in 'a'..'z' || next == '.' || next == '+' || next == '-' || next == '_') {
                builder.append(next)
                skip()
                continue
            }
            break
        }
        return builder.toString()
    }

    fun readQuotedString(): String {
        if (!canRead() || !peekExpected('"', '\'')) {
            throw StringCommandParseException(this, peek().toString(), "String is not quoted!")
        }
        return readStringUntil(read())
    }

    fun readNumericString(): String {
        val builder = StringBuilder()
        while (canRead()) {
            val next = peek()
            if (isCharNumeric(next)) {
                builder.append(next)
                skip()
                continue
            }
            break
        }
        return builder.toString()
    }

    fun readProperty(): Pair<String, String> {
        skipWhitespaces()
        val key = readString()
        skipWhitespaces()
        if (read() != '=') {
            throw BadPropertyMapCommandParseException(this, key, "Not a property string!")
        }
        skipWhitespaces()
        val value = readString()
        skipWhitespaces()
        return Pair(key, value)
    }

    fun readProperties(): Map<String, String> {
        if (peek() != '[') {
            throw BadPropertyMapCommandParseException(this, peek().toString(), "Not a property map!")
        }
        val ret: MutableMap<String, String> = HashMap()
        skip()
        skipWhitespaces()
        if (peek() == ']') {
            return ret
        }
        var property = readProperty()
        ret[property.key] = property.value
        while (peek() == ',') {
            property = readProperty()
            if (ret.containsKey(property.key)) {
                throw DuplicatedPropertyKeyCommandParseException(this, property.key)
            }
            ret[property.key] = property.value
        }
        if (peek() != ']') {
            throw BadPropertyMapCommandParseException(this, peek().toString(), "Bad property map ending!")
        }
        skip()
        return ret
    }

    fun readResourceLocation(): Pair<String, ResourceLocation> {
        val builder = StringBuilder()
        while (canRead()) {
            val next = peek()
            if (next >= '0' && next <= '9' || next >= 'a' && next <= 'z' || next == '.' || next == '-' || next == '_' || next == ':' || next == '/') {
                builder.append(next)
                skip()
                continue
            }
            break
        }
        val resourceLocation = builder.toString()
        return try {
            Pair(resourceLocation, ResourceLocation.getResourceLocation(builder.toString()))
        } catch (exception: IllegalArgumentException) {
            throw InvalidResourceLocationCommandParseException(this, resourceLocation, exception)
        }
    }

    fun readString(): String {
        if (!canRead()) {
            return ""
        }
        return if (peekExpected('"', '\'')) {
            readQuotedString()
        } else readUnquotedString()
    }

    fun peekExpected(vararg expected: Char): Boolean {
        val next = peek()
        for (c in expected) {
            if (next == c) {
                return true
            }
        }
        return false
    }

    private fun readStringUntil(requiresTerminator: Boolean, terminator: Char): String {
        return readStringUntil(requiresTerminator, *charArrayOf(terminator)).key
    }

    private fun readStringUntil(requiresTerminator: Boolean, vararg terminators: Char): Pair<String, Char> {
        val builder = StringBuilder()
        var isNextCharEscaped = false
        while (canRead()) {
            val read = read()
            if (read == '\\') {
                isNextCharEscaped = true
                continue
            }
            if (isNextCharEscaped) {
                builder.append(read)
                isNextCharEscaped = false
                continue
            }
            for (terminator in terminators) {
                if (read == terminator) {
                    return Pair(builder.toString(), terminator)
                }
            }
            builder.append(read)
        }
        if (requiresTerminator) {
            throw StringCommandParseException(this, builder.toString(), "Terminator(s) not found in string!")
        }
        return Pair(builder.toString(), 0.toChar())
    }

    fun readBoolean(): Boolean {
        val read = readString()
        if (read == "true") {
            return true
        }
        if (read == "false") {
            return true
        }
        throw BooleanCommandParseException(this, read)
    }

    fun readInt(): Int {
        val numericString = readNumericString()
        return try {
            numericString.toInt()
        } catch (exception: NumberFormatException) {
            throw IntegerCommandParseException(this, numericString, exception)
        }
    }

    fun readLong(): Long {
        val numericString = readNumericString()
        return try {
            numericString.toLong()
        } catch (exception: NumberFormatException) {
            throw LongCommandParseException(this, numericString, exception)
        }
    }

    fun readFloat(): Float {
        val numericString = readNumericString()
        return try {
            numericString.toFloat()
        } catch (exception: NumberFormatException) {
            throw FloatCommandParseException(this, numericString, exception)
        }
    }

    fun readDouble(): Double {
        val numericString = readNumericString()
        return try {
            numericString.toDouble()
        } catch (exception: NumberFormatException) {
            throw DoubleCommandParseException(this, numericString, exception)
        }
    }

    fun readStringUntilOrEnd(vararg terminators: Char): Pair<String, Char> {
        return try {
            readStringUntil(false, *terminators)
        } catch (e: StringCommandParseException) {
            e.printStackTrace()
            throw RuntimeException(e)
        }
    }

    fun readStringUntil(vararg terminators: Char): Pair<String, Char> {
        return readStringUntil(true, *terminators)
    }

    fun readStringUntilOrEnd(terminator: Char): String {
        return try {
            readStringUntil(false, terminator)
        } catch (e: StringCommandParseException) {
            e.printStackTrace()
            throw RuntimeException(e)
        }
    }

    fun readStringUntil(terminator: Char): String {
        return readStringUntil(true, terminator)
    }

    fun readNBTListTag(): MutableList<Any> {
        skipWhitespaces()
        if (peek() != '[') {
            throw ListTagBadFormatCommandParseException(this, peek().toString(), "Not a list tag!")
        }
        skip()
        val listTag: MutableList<Any> = mutableListOf()
        skipWhitespaces()
        if (peek() == ']') {
            skip()
            return listTag
        }
        while (canRead()) {
            val start = cursor
            try {
                listTag.add(readNBTTag())
            } catch (exception: IllegalArgumentException) {
                throw ListTagBadFormatCommandParseException(this, string.substring(start, cursor), exception)
            }
            skipWhitespaces()
            if (peek() == ',') {
                skip()
                skipWhitespaces()
            }
            if (peek() == ']') {
                skip()
                return listTag
            }
        }
        throw ListTagBadFormatCommandParseException(this, peek().toString(), "No closing tag!")
    }

    fun readNBTTag(): Any {
        // ToDo: Array tags
        skipWhitespaces()
        if (peek() == '[') {
            return readNBTListTag()
        }
        if (peek() == '{') {
            return readNBTCompoundTag()
        }
        val data = readString().lowercase(Locale.ROOT)
        if (data == "true") {
            return true
        }
        if (data == "false") {
            return false
        }
        try {
            if (NBT_PATTERN_BYTE.matcher(data).matches()) {
                return data.substring(0, data.length - 1).toByte()
            }
            if (NBT_PATTERN_SHORT.matcher(data).matches()) {
                return data.substring(0, data.length - 1).toShort()
            }
            if (NBT_PATTERN_LONG.matcher(data).matches()) {
                return data.substring(0, data.length - 1).toLong()
            }
            if (NBT_PATTERN_INT.matcher(data).matches()) {
                return data.toInt()
            }
            if (NBT_PATTERN_FLOAT.matcher(data).matches()) {
                return data.substring(0, data.length - 1).toFloat()
            }
            if (NBT_PATTERN_DOUBLE.matcher(data).matches()) {
                return if (data.endsWith("d")) data.substring(0, data.length - 1) else data.toDouble()
            }
        } catch (ignored: NumberFormatException) {
        }
        return data
    }

    fun readNBTCompoundTag(): MutableMap<String, Any> {
        if (peek() != '{') {
            throw CompoundTagBadFormatCommandParseException(this, peek().toString(), "Not a compound tag!")
        }
        val compoundTag: MutableMap<String, Any> = mutableMapOf()
        skip()
        skipWhitespaces()
        if (peek() == '}') {
            return compoundTag
        }
        while (canRead()) {
            val key = readString()
            skipWhitespaces()
            if (peek() != ':') {
                throw CompoundTagBadFormatCommandParseException(this, peek().toString(), "Invalid map char!")
            }
            skip()
            val value: Any = readNBTTag()
            compoundTag[key] = value
            skipWhitespaces()
            val nextChar = read()
            if (nextChar == '}') {
                return compoundTag
            } else {
                skipWhitespaces()
                if (nextChar != ',') {
                    throw CompoundTagBadFormatCommandParseException(this, nextChar.toString(), "Invalid map char!")
                }
            }
        }
        throw CompoundTagBadFormatCommandParseException(this, peek().toString(), "No closing tag!")
    }

    fun readJson(): JsonElement {
        return try {
            val jsonReader = JsonReader(StringReader(remaining))
            jsonReader.isLenient = false
            val json = GSON.fromJson<JsonObject>(jsonReader, JsonObject::class.java)
            skip(Util.getJsonReaderPosition(jsonReader) - 1)
            json
        } catch (exception: JsonParseException) {
            throw InvalidJSONCommandParseException(this, peek().toString(), exception)
        }
    }

    fun readExpected(vararg expected: Char): Boolean {
        val ret = peekExpected(*expected)
        skip()
        return ret
    }

    fun readRemaining(): String {
        val ret = string.substring(cursor)
        cursor = string.length
        return ret
    }

    fun peekRemaining(): String {
        return string.substring(cursor)
    }

    override fun toString(): String {
        return if (canRead()) {
            String.format("position=%d/%d: \"%s\"", cursor, string.length, peekRemaining())
        } else String.format("position=%d/%d", cursor, string.length)
    }

    companion object {
        private val NBT_PATTERN_INT = Pattern.compile("[-+]?(0|[1-9][0-9]*)")
        private val NBT_PATTERN_BYTE = Pattern.compile(NBT_PATTERN_INT.pattern() + "b")
        private val NBT_PATTERN_SHORT = Pattern.compile(NBT_PATTERN_INT.pattern() + "s")
        private val NBT_PATTERN_LONG = Pattern.compile(NBT_PATTERN_INT.pattern() + "l")
        private val NBT_PATTERN_FLOAT = Pattern.compile(NBT_PATTERN_INT.pattern() + "([.][0-9]*)?f")
        private val NBT_PATTERN_DOUBLE = Pattern.compile(NBT_PATTERN_INT.pattern() + "([.][0-9]*)?d?")
        private val GSON = Gson()

        fun isCharNumeric(c: Char): Boolean {
            return c in '0'..'9' || c == '-' || c == '.'
        }
    }
}
