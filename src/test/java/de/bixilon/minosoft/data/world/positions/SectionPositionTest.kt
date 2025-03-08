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

package de.bixilon.minosoft.data.world.positions

import de.bixilon.minosoft.config.DebugOptions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class SectionPositionTest {

    @Test
    fun `init correct min`() {
        val position = SectionPosition(-1875000, -128, -1875000)
    }

    @Test
    fun `init correct max`() {
        val position = SectionPosition(1875000, 127, 1875000)
    }

    @Test
    fun `init badly`() {
        if (!DebugOptions.VERIFY_COORDINATES) return
        assertThrows<AssertionError> { SectionPosition(-1875001, 128, -1875001) }
    }

    @Test
    fun `correct positive x`() {
        val position = SectionPosition(2, 0xF, 0xF)
        assertEquals(position.x, 2)
    }

    @Test
    fun `correct positive x large`() {
        val position = SectionPosition(1875000, 0xF, 0xF)
        assertEquals(position.x, 1875000)
    }

    @Test
    fun `correct negative x`() {
        val position = SectionPosition(-2, 0xF, 0xF)
        assertEquals(position.x, -2)
    }

    @Test
    fun `correct negative x large`() {
        val position = SectionPosition(-1875000, 0xF, 0xF)
        assertEquals(position.x, -1875000)
    }

    @Test
    fun `correct plus x`() {
        val position = SectionPosition(2, 0xF, 0xF)
        assertEquals(position.plusX().x, 3)
    }

    @Test
    fun `correct plus 2 x`() {
        val position = SectionPosition(2, 0xF, 0xF)
        assertEquals(position.plusX(2).x, 4)
    }

    @Test
    fun `correct minus x`() {
        val position = SectionPosition(2, 0xF, 0xF)
        assertEquals(position.minusX().x, 1)
    }

    @Test
    fun `correct negative y`() {
        val position = SectionPosition(0xF, -4, 0xF)
        assertEquals(position.y, -4)
    }

    @Test
    fun `correct negative y large`() {
        val position = SectionPosition(123, -128, 0xF)
        assertEquals(position.y, -128)
    }

    @Test
    fun `correct positive y`() {
        val position = SectionPosition(0xF, 100, 0xF)
        assertEquals(position.y, 100)
    }

    @Test
    fun `correct positive y large`() {
        val position = SectionPosition(0xF, 127, 0xF)
        assertEquals(position.y, 127)
    }

    @Test
    fun `correct plus y`() {
        val position = SectionPosition(0xF, 2, 0xF)
        assertEquals(position.plusY().y, 3)
    }

    @Test
    fun `correct plus 2 y`() {
        val position = SectionPosition(0xF, 2, 0xF)
        assertEquals(position.plusY(2).y, 4)
    }

    @Test
    fun `correct minus y`() {
        val position = SectionPosition(0xF, 2, 0xF)
        assertEquals(position.minusY().y, 1)
    }

    @Test
    fun `correct positive z`() {
        val position = SectionPosition(0xF, 0xF, 4)
        assertEquals(position.z, 4)
    }

    @Test
    fun `correct positive z large`() {
        val position = SectionPosition(0, 0, 1875000)
        assertEquals(position.z, 1875000)
    }

    @Test
    fun `correct negative z`() {
        val position = SectionPosition(0xF, 0xF, -4)
        assertEquals(position.z, -4)
    }

    @Test
    fun `correct negative z large`() {
        val position = SectionPosition(0, 0, -1875000)
        assertEquals(position.z, -1875000)
    }


    @Test
    fun `correct plus z`() {
        val position = SectionPosition(0xF, 0xF, 2)
        assertEquals(position.plusZ().z, 3)
    }

    @Test
    fun `correct plus 2 z`() {
        val position = SectionPosition(0xF, 0xF, 2)
        assertEquals(position.plusZ(2).z, 4)
    }

    @Test
    fun `correct minus z`() {
        val position = SectionPosition(0xF, 0xF, 2)
        assertEquals(position.minusZ().z, 1)
    }

    @Test
    fun `unary minus`() {
        val position = -SectionPosition(2, 2, 2)
        assertEquals(position.x, -2)
        assertEquals(position.y, -2)
        assertEquals(position.z, -2)
    }

    @Test
    fun `unary plus`() {
        val position = +SectionPosition(2, 2, 2)
        assertEquals(position.x, 2)
        assertEquals(position.y, 2)
        assertEquals(position.z, 2)
    }
}
