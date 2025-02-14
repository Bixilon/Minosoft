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

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class InSectionPositionTest {


    @Test
    fun `init correct min`() {
        val position = InSectionPosition(0, 0, 0)
    }

    @Test
    fun `init correct max`() {
        val position = InSectionPosition(15, 15, 15)
    }

    @Test
    fun `init badly`() {
        assertThrows<AssertionError> { InSectionPosition(-1, -1, -1) }
    }

    @Test
    fun `correct x`() {
        val position = InSectionPosition(2, 0xF, 0xF)
        assertEquals(position.x, 2)
    }

    @Test
    fun `correct plus x`() {
        val position = InSectionPosition(2, 0xF, 0xF)
        assertEquals(position.plusX().x, 3)
    }

    @Test
    fun `correct plus 2 x`() {
        val position = InSectionPosition(2, 0xF, 0xF)
        assertEquals(position.plusX(2).x, 4)
    }

    @Test
    fun `correct minus x`() {
        val position = InSectionPosition(2, 0xF, 0xF)
        assertEquals(position.minusX().x, 1)
    }

    @Test
    fun `correct y`() {
        val position = InSectionPosition(0xF, 3, 0xF)
        assertEquals(position.y, 3)
    }

    @Test
    fun `correct plus y`() {
        val position = InSectionPosition(0xF, 2, 0xF)
        assertEquals(position.plusY().y, 3)
    }

    @Test
    fun `correct plus 2 y`() {
        val position = InSectionPosition(0xF, 2, 0xF)
        assertEquals(position.plusY(2).y, 4)
    }

    @Test
    fun `correct minus y`() {
        val position = InSectionPosition(0xF, 2, 0xF)
        assertEquals(position.minusY().y, 1)
    }

    @Test
    fun `correct z`() {
        val position = InSectionPosition(0xF, 0xF, 4)
        assertEquals(position.z, 4)
    }

    @Test
    fun `correct plus z`() {
        val position = InSectionPosition(0xF, 0xF, 2)
        assertEquals(position.plusZ().z, 3)
    }

    @Test
    fun `correct plus 2 z`() {
        val position = InSectionPosition(0xF, 0xF, 2)
        assertEquals(position.plusZ(2).z, 4)
    }

    @Test
    fun `correct minus z`() {
        val position = InSectionPosition(0xF, 0xF, 2)
        assertEquals(position.minusZ().z, 1)
    }
}
