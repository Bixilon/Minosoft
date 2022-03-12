/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.minosoft.util.Pair

@Deprecated("To be replaced")
open class CommandStringReader {
    val string: String
    var cursor = 0

    constructor(string: String) {
        this.string = string
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
            throw IllegalStateException()
        }
        return readStringUntil(read())
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
            throw IllegalStateException()
        }
        return Pair(builder.toString(), 0.toChar())
    }

    fun readStringUntil(terminator: Char): String {
        return readStringUntil(true, terminator)
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
        fun isCharNumeric(c: Char): Boolean {
            return c in '0'..'9' || c == '-' || c == '.'
        }
    }
}
