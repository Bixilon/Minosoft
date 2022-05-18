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

package de.bixilon.minosoft.commands.util

import de.bixilon.minosoft.commands.errors.reader.*

open class CommandReader(val string: String) {
    var pointer = 0
    val length = string.length

    fun canPeek(ignoreWhitespaces: Boolean = true): Boolean {
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
        if (!canPeekNext()) {
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

    fun skipWhitespaces(): Int {
        val count = peekWhitespaces()
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
            val char = read(false) ?: return builder.toString()
            if (Character.isWhitespace(char)) {
                return builder.toString()
            }
            builder.appendCodePoint(char)
        }
    }

    fun readQuotedString(): String? {
        skipWhitespaces()
        val start = pointer
        if (!canPeekNext()) {
            return null
        }
        unsafeRead(STRING_QUOTE)
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
                if (read != STRING_QUOTE) {
                    throw UnexpectedBackslashError(this, start, pointer, read)
                }
                string.append('"')
                skipNextChar = false
                continue
            }
            if (read == STRING_QUOTE) {
                return string.toString()
            }
            string.appendCodePoint(read)
        }
    }

    fun readString(): String? {
        skipWhitespaces()
        val start = peekNext() ?: return null
        if (start == STRING_QUOTE) {
            return readQuotedString()
        }
        return readUnquotedString()
    }

    fun readRest(): String? {
        if (!canPeekNext()) {
            return null
        }
        val string = string.substring(pointer, string.length)
        pointer = string.length
        return string
    }

    fun <T> readResult(reader: CommandReader.() -> T): ReadResult<T> {
        val start = pointer
        val result = reader(this)
        val end = pointer
        val read = string.substring(start, end)

        return ReadResult(start, end, read, result)
    }

    companion object {
        const val STRING_QUOTE = '"'.code
    }
}
