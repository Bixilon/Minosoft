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

package de.bixilon.minosoft.gui.rendering.camera.occlusion

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SectionPositionSetTest {

    private fun create(minSection: Int = 0) = SectionPositionSet(ChunkPosition(20, 30), Vec2i(6, 8), minSection, 8)

    @Test
    fun empty() {
        val set = create()

        assertFalse(SectionPosition(20, 1, 30) in set)
    }

    @Test
    fun `get out of bounds x`() {
        val set = create()

        assertFalse(SectionPosition(200, 1, 30) in set)
    }

    @Test
    fun `get out of bounds y`() {
        val set = create()

        assertFalse(SectionPosition(20, 100, 30) in set)
    }

    @Test
    fun `get out of bounds z`() {
        val set = create()

        assertFalse(SectionPosition(20, 1, 300) in set)
    }

    @Test
    fun `set out of bounds x`() {
        val set = create()
        set += SectionPosition(200, 1, 30)

        assertFalse(SectionPosition(200, 1, 30) in set)
    }

    @Test
    fun `set out of bounds y`() {
        val set = create()
        set += SectionPosition(20, 100, 30)

        assertFalse(SectionPosition(20, 100, 30) in set)
    }

    @Test
    fun `set out of bounds z`() {
        val set = create()
        set += SectionPosition(20, 1, 300)

        assertFalse(SectionPosition(20, 1, 300) in set)
    }

    @Test
    fun `set and get`() {
        val set = create()
        set += SectionPosition(20, 1, 30)

        assertTrue(SectionPosition(20, 1, 30) in set)
    }

    @Test
    fun `set remove and get`() {
        val set = create()
        set += SectionPosition(20, 1, 30)
        set -= SectionPosition(20, 1, 30)

        assertFalse(SectionPosition(20, 1, 30) in set)
    }

    @Test
    fun `set negative x`() {
        val set = create()
        set += SectionPosition(19, 1, 30)

        assertTrue(SectionPosition(19, 1, 30) in set)
    }

    @Test
    fun `set negative z`() {
        val set = create()
        set += SectionPosition(20, 1, 29)

        assertTrue(SectionPosition(20, 1, 29) in set)
    }

    @Test
    fun `set negative xz`() {
        val set = create()
        set += SectionPosition(19, 1, 29)

        assertTrue(SectionPosition(19, 1, 29) in set)
    }

    @Test
    fun `set positive xz`() {
        val set = create()
        set += SectionPosition(21, 1, 31)

        assertTrue(SectionPosition(21, 1, 31) in set)
    }

    @Test
    fun `set different height`() {
        val set = create()
        set += SectionPosition(21, 1, 31)

        assertFalse(SectionPosition(21, 2, 31) in set)
    }

    @Test
    fun `set negative y`() {
        val set = create(minSection = -4)
        set += SectionPosition(21, -2, 31)

        assertTrue(SectionPosition(21, -2, 31) in set)
    }

    @Test
    fun `set positive y with negative min y`() {
        val set = create(minSection = -4)
        set += SectionPosition(21, 2, 31)

        assertTrue(SectionPosition(21, 2, 31) in set)
    }
}
