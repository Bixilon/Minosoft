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

import de.bixilon.minosoft.commands.errors.reader.OutOfBoundsError
import de.bixilon.minosoft.commands.errors.reader.number.NegativeNumberError
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class CommandReaderTest {
    @Test
    fun `Peek char`() {
        val reader = CommandReader("test")
        assertEquals(reader.peek(), 't'.code)
        assertEquals(reader.peek(), 't'.code)
    }

    @Test
    fun `Read test`() {
        val reader = CommandReader("test")
        assertEquals(reader.read(), 't'.code)
        assertEquals(reader.read(), 'e'.code)
        assertEquals(reader.read(), 's'.code)
        assertEquals(reader.read(), 't'.code)
    }

    @Test
    fun `Check OutOfBounds null`() {
        val reader = CommandReader("test")
        assertEquals(reader.read(), 't'.code)
        assertEquals(reader.read(), 'e'.code)
        assertEquals(reader.read(), 's'.code)
        assertEquals(reader.read(), 't'.code)
        assertEquals(reader.read(), null)
    }

    @Test
    fun `Check OutOfBounds throw`() {
        val reader = CommandReader("test")
        assertEquals(reader.read(), 't'.code)
        assertEquals(reader.read(), 'e'.code)
        assertEquals(reader.read(), 's'.code)
        assertEquals(reader.read(), 't'.code)
        assertThrows<OutOfBoundsError> { reader.unsafeRead() }
    }

    @Test
    fun `Check unquotedString read`() {
        val reader = CommandReader("test")
        assertEquals(reader.readString(), "test")
    }

    @Test
    fun `Check unquotedString read 2`() {
        val reader = CommandReader("test test2")
        assertEquals(reader.readString(), "test")
        assertEquals(reader.readString(), "test2")
    }

    @Test
    fun `Read quoted string`() {
        val reader = CommandReader("\"test\"")
        assertEquals(reader.readString(), "test")
    }

    @Test
    fun `Read separated quoted string`() {
        val reader = CommandReader("\"test test\"")
        assertEquals(reader.readString(), "test test")
    }

    @Test
    fun `Read escaped quoted string`() {
        val reader = CommandReader("\"test \\\"test\"")
        assertEquals(reader.readString(), "test \"test")
    }

    @Test
    fun readNumeric() {
        val reader = CommandReader("123")
        assertEquals(reader.readNumeric(), "123")
    }

    @Test
    fun read2Numeric() {
        val reader = CommandReader("123 456")
        assertEquals(reader.readNumeric(), "123")
        assertEquals(reader.readNumeric(), "456")
    }

    @Test
    fun readNegative() {
        val reader = CommandReader("-7813")
        assertEquals(reader.readNumeric(), "-7813")
    }

    @Test
    fun readNoNegative() {
        val reader = CommandReader("-7813")
        assertThrows<NegativeNumberError> { reader.readNumeric(negative = false) }
    }

    @Test
    fun readDecimal() {
        val reader = CommandReader("98.76")
        assertEquals(reader.readNumeric(), "98.76")
    }

    @Test
    fun readNoDecimal() {
        val reader = CommandReader("98.76")
        assertEquals(reader.readNumeric(decimal = false), "98")
    }

    @Test
    fun readWord() {
        val reader = CommandReader("word")
        assertEquals(reader.readWord(), "word")
    }

    @Test
    fun readWhitespacedWord() {
        val reader = CommandReader("    word")
        assertEquals(reader.readWord(), "word")
    }

    @Test
    fun read2Words() {
        val reader = CommandReader("first second")
        assertEquals(reader.readWord(), "first")
        assertEquals(reader.readWord(), "second")
    }

    @Test
    fun read2Words2() {
        val reader = CommandReader("first!second")
        assertEquals(reader.readWord(), "first")
        assertEquals(reader.read(), '!'.code)
        assertEquals(reader.readWord(), "second")
    }

    @Test
    fun read2Words3() {
        val reader = CommandReader("first=second")
        assertEquals(reader.readWord(), "first")
        assertEquals(reader.read(), '='.code)
        assertEquals(reader.readWord(), "second")
    }

    @Test
    fun readInvalidWord() {
        val reader = CommandReader("=")
        assertEquals(reader.readWord(), null)
    }

    @Test
    fun readNegateable() {
        val reader = CommandReader("!not")
        val (read, negated) = reader.readNegateable { readWord() }!!
        assertEquals(read, "not")
        assertTrue(negated)
    }

    @Test
    fun readNotNegateable() {
        val reader = CommandReader("not")
        val (read, negated) = reader.readNegateable { readWord() }!!
        assertEquals(read, "not")
        assertFalse(negated)
    }

    @Test
    fun readEmptyJson() {
        val reader = CommandReader("{}")
        assertEquals(reader.readJson(), emptyMap())
    }

    @Test
    fun readBasicJson() {
        val reader = CommandReader("""{"test": 123}""")
        assertEquals(reader.readJson(), mapOf("test" to 123))
    }

    @Test
    fun readJsonWithTrailingData() {
        val reader = CommandReader("""{"test": 123} abc""")
        assertEquals(reader.readJson(), mapOf("test" to 123))
        assertEquals(reader.readString(), "abc")
    }
}
