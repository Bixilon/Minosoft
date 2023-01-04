/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.commands.util

import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.commands.errors.reader.*
import de.bixilon.minosoft.commands.errors.reader.number.NegativeNumberError
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.json.Jackson

open class StringReader(val string: String) {
    var pointer = 0
    val length = string.length

    fun canPeek(pointer: Int = this.pointer, ignoreWhitespaces: Boolean = true): Boolean {
        if (pointer >= length) {
            return false
        }
        val whitespaces = if (ignoreWhitespaces) peekWhitespaces() else 0
        return pointer + whitespaces < length
    }

    fun checkPeekNext(pointer: Int = this.pointer) {
        if (!canPeekNext(pointer)) {
            throw OutOfBoundsError(this, length - 1)
        }
    }

    fun canPeekNext(pointer: Int = this.pointer): Boolean {
        return pointer < length
    }

    fun peekNext(pointer: Int = this.pointer): Int? {
        if (!canPeekNext(pointer)) {
            return null
        }
        return string.codePointAt(pointer)
    }

    fun readNext(): Int? {
        val next = peekNext(pointer) ?: return null
        pointer++
        return next
    }

    fun unsafePeekNext(): Int {
        return peekNext() ?: throw OutOfBoundsError(this, length - 1)
    }

    fun unsafeReadNext(): Int {
        return readNext() ?: throw OutOfBoundsError(this, length - 1)
    }

    fun peekWhitespaces(): Int {
        var count = 0
        while (true) {
            val peek = peekNext(pointer + count) ?: return count
            if (!Character.isWhitespace(peek)) {
                break
            }
            count++
        }
        return count
    }

    fun skipWhitespaces(minimum: Int = 0): Int {
        val count = peekWhitespaces()
        if (count < minimum) {
            throw ExpectedWhitespaceError(this, pointer, minimum, count)
        }
        pointer += count
        return count
    }

    fun peek(ignoreWhitespaces: Boolean = true): Int? {
        if (ignoreWhitespaces) {
            skipWhitespaces()
        }
        return peekNext()
    }

    fun read(ignoreWhitespaces: Boolean = true): Int? {
        if (ignoreWhitespaces) {
            skipWhitespaces()
        }
        val peek = peekNext() ?: return null
        pointer++
        return peek
    }

    fun unsafePeek(ignoreWhitespaces: Boolean = true): Int {
        if (ignoreWhitespaces) {
            skipWhitespaces()
        }
        return unsafePeekNext()
    }

    fun unsafeRead(ignoreWhitespaces: Boolean = true): Int {
        val peek = unsafePeek(ignoreWhitespaces)
        pointer++
        return peek
    }

    fun peek(vararg chars: Int, ignoreWhitespaces: Boolean = true): Int? {
        val peek = peek(ignoreWhitespaces) ?: return null
        if (peek !in chars) {
            return null
        }
        return peek
    }

    fun read(vararg chars: Int, ignoreWhitespaces: Boolean = true): Int? {
        val read = read(ignoreWhitespaces) ?: return null
        if (read !in chars) {
            return null
        }
        return read
    }

    fun unsafePeek(vararg chars: Int, ignoreWhitespaces: Boolean = true): Int {
        val peek = unsafePeek(ignoreWhitespaces)
        if (peek !in chars) {
            throw InvalidPeekError(this, pointer, peek, chars)
        }
        return peek
    }

    fun unsafeRead(vararg chars: Int, ignoreWhitespaces: Boolean = true): Int {
        val read = unsafeRead(ignoreWhitespaces)
        if (read !in chars) {
            throw InvalidReadError(this, pointer, read, chars)
        }
        return read
    }

    fun readUnquotedString(): String? {
        val builder = StringBuilder()
        skipWhitespaces()
        if (!canPeekNext()) {
            return null
        }
        while (true) {
            val char = peekNext() ?: return builder.toString()

            if (Character.isWhitespace(char)) {
                return builder.toString()
            }
            builder.appendCodePoint(char)
            pointer++
        }
    }

    fun readQuotedString(): String? {
        skipWhitespaces()
        val start = pointer
        if (!canPeekNext()) {
            return null
        }
        val quoteStart = unsafeRead(STRING_QUOTE, STRING_SINGLE_QUOTE)
        val string = StringBuilder()
        var skipNextChar = false
        while (true) {
            val read = readNext() ?: throw UnfinishedQuotedStringError(this, pointer)
            if (read == '\\'.code) {
                if (skipNextChar) {
                    string.append('\\')
                    skipNextChar = false
                    continue
                }
                skipNextChar = true
                continue
            }
            if (skipNextChar) {
                if (read != quoteStart) {
                    throw UnexpectedBackslashError(this, start, pointer, read)
                }
                string.append('"')
                skipNextChar = false
                continue
            }
            if (read == quoteStart) {
                return string.toString()
            }
            string.appendCodePoint(read)
        }
    }

    fun readString(skipWhitespace: Boolean = true): String? {
        if (skipWhitespace) {
            skipWhitespaces()
        }
        val start = peekNext() ?: return null
        if (start == STRING_QUOTE || start == STRING_SINGLE_QUOTE) {
            return readQuotedString()
        }
        return readUnquotedString()
    }

    fun readRest(): String? {
        if (!canPeekNext()) {
            return null
        }
        val string = string.substring(pointer, string.length)
        pointer = this.string.length
        return string
    }

    fun readUntil(vararg chars: Int, required: Boolean = false): String? {
        if (!canPeekNext()) {
            return null
        }
        val builder = StringBuilder()
        while (true) {
            val peek = peekNext()
            if (peek == null) {
                if (required) {
                    throw OutOfBoundsError(this, length - 1)
                }
                return builder.toString()
            }
            if (peek in chars) {
                return builder.toString()
            }
            builder.appendCodePoint(peek)
            pointer++
        }
    }

    fun readNumeric(decimal: Boolean = true, negative: Boolean = true): String? {
        if (!canPeek()) {
            return null
        }
        skipWhitespaces()
        val builder = StringBuilder()
        while (true) {
            val peek = peekNext() ?: break
            if (peek in '0'.code..'9'.code) {
                builder.appendCodePoint(peek)
                pointer++
                continue
            } else if (peek == '-'.code && builder.isEmpty()) {
                if (!negative) {
                    throw NegativeNumberError(this, pointer)
                }
                builder.appendCodePoint(peek)
                pointer++
            } else if (decimal && peek == '.'.code && '.' !in builder) {
                builder.appendCodePoint(peek)
                pointer++
            } else {
                break
            }
        }
        return builder.toString()
    }

    fun readWord(skipWhitespace: Boolean = true): String? {
        if (!canPeek()) {
            return null
        }
        val builder = StringBuilder()
        if (skipWhitespace) {
            skipWhitespaces()
        }
        while (true) {
            val peek = peekNext() ?: break
            if (peek.isWord()) {
                builder.appendCodePoint(peek)
                pointer++
                continue
            } else {
                break
            }
        }
        if (builder.isEmpty()) {
            return null
        }
        return builder.toString()
    }

    fun readResourceLocation(): ResourceLocation? {
        val namespace = readWord() ?: return null
        if (peek() != ':'.code) {
            return namespace.toResourceLocation()
        }
        read()
        val path = readWord() ?: return null

        return ResourceLocation(namespace, path)
    }

    fun <T> readResult(reader: StringReader.() -> T): ReadResult<T> {
        val start = pointer
        val result = reader(this)
        val end = pointer
        val read = string.substring(start, end)

        return ReadResult(start, end, read, result)
    }

    fun readJson(): JsonObject {
        skipWhitespaces()
        return Jackson.MAPPER.readValue(JsonReader(this), Jackson.JSON_MAP_TYPE)
    }

    override fun toString(): String {
        return string.substring(pointer, string.length)
    }


    fun peekRemaining(): String? {
        if (pointer == length) {
            return null
        }
        return string.substring(pointer, string.length)
    }

    companion object {
        const val STRING_QUOTE = '"'.code
        const val STRING_SINGLE_QUOTE = '\''.code


        fun Int.isWord(): Boolean {
            return this in '0'.code..'9'.code || this in 'a'.code..'z'.code || this in 'A'.code..'Z'.code || this == '_'.code || this == '-'.code || this == '/'.code
        }
    }
}
