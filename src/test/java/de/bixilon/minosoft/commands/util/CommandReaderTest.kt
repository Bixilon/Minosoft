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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

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
        assertThrows(OutOfBoundsError::class.java) { reader.unsafeRead() }
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
}
