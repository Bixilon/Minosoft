/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.chunk.light.types

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LightLevelTest {

    @Test
    fun empty() {
        assertEquals(LightLevel.EMPTY.block, 0)
        assertEquals(LightLevel.EMPTY.sky, 0)
    }

    @Test
    fun max() {
        assertEquals(LightLevel.MAX.block, 15)
        assertEquals(LightLevel.MAX.sky, 15)
    }

    @Test
    fun `correct blocklight without skylight`() {
        val level = LightLevel(block = 12, sky = 0)
        assertEquals(level.block, 12)
        assertEquals(level.sky, 0)
    }

    @Test
    fun `correct skylight without blocklight`() {
        val level = LightLevel(block = 0, sky = 11)
        assertEquals(level.block, 0)
        assertEquals(level.sky, 11)
    }

    @Test
    fun `correct empty index`() {
        assertEquals(LightLevel.EMPTY.index, 0x00)
    }

    @Test
    fun `correct max index`() {
        assertEquals(LightLevel.MAX.index, 0xFF)
    }

    @Test
    fun `max level`() {
        val a = LightLevel(2, 3)
        val b = LightLevel(4, 2)
        val max = a.max(b)
        assertEquals(max.block, 4)
        assertEquals(max.sky, 3)
    }
}
