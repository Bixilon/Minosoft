/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.kmath.vec.vec4.i

import de.bixilon.kmath.vec.VecUtil.VERIFY_VECTORS
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class SVec4iTest {

    @Test
    fun `init correct min`() {
        SVec4i(-127, -127, -127, -127)
    }

    @Test
    fun `init correct max`() {
        SVec4i(127, 127, 127, 127)
    }

    @Test
    fun `init badly`() {
        if (!VERIFY_VECTORS) return
        assertThrows<AssertionError> { SVec4i(-40_000_000, -1000, -40_000_000, -40123) }
    }

    @Test
    fun `correct positive x`() {
        val position = SVec4i(2, 0xF, 0xF, 0xF)
        assertEquals(position.x, 2)
    }

    @Test
    fun `correct positive x large`() {
        val position = SVec4i(127, 0xF, 0xF, 0xF)
        assertEquals(position.x, 127)
    }

    @Test
    fun `correct negative x`() {
        val position = SVec4i(-2, 0xF, 0xF, 0xF)
        assertEquals(position.x, -2)
    }

    @Test
    fun `correct negative x large`() {
        val position = SVec4i(-127, 0xF, 0xF, 0xF)
        assertEquals(position.x, -127)
    }

    @Test
    fun `correct plus x`() {
        val position = SVec4i(2, 0xF, 0xF, 0xF)
        assertEquals(position.plusX().x, 3)
    }

    @Test
    fun `correct plus 2 x`() {
        val position = SVec4i(2, 0xF, 0xF, 0xF)
        assertEquals(position.plusX(2).x, 4)
    }

    @Test
    fun `correct minus x`() {
        val position = SVec4i(2, 0xF, 0xF, 0xF)
        assertEquals(position.minusX().x, 1)
    }

    @Test
    fun `correct negative y`() {
        val position = SVec4i(0xF, -29, 0xF, 0xF)
        assertEquals(position.y, -29)
    }

    @Test
    fun `correct negative y large`() {
        val position = SVec4i(42, -127, 0xF, 0xF)
        assertEquals(position.y, -127)
    }

    @Test
    fun `correct positive y`() {
        val position = SVec4i(0xF, 29, 0xF, 0xF)
        assertEquals(position.y, 29)
    }

    @Test
    fun `correct positive y large`() {
        val position = SVec4i(0xF, 127, 0xF, 0xF)
        assertEquals(position.y, 127)
    }

    @Test
    fun `correct plus y`() {
        val position = SVec4i(0xF, 2, 0xF, 0xF)
        assertEquals(position.plusY().y, 3)
    }

    @Test
    fun `correct plus 2 y`() {
        val position = SVec4i(0xF, 2, 0xF, 0xF)
        assertEquals(position.plusY(2).y, 4)
    }

    @Test
    fun `correct minus y`() {
        val position = SVec4i(0xF, 2, 0xF, 0xF)
        assertEquals(position.minusY().y, 1)
    }

    @Test
    fun `correct positive z`() {
        val position = SVec4i(0xF, 0xF, 4, 0xF)
        assertEquals(position.z, 4)
    }

    @Test
    fun `correct positive z large`() {
        val position = SVec4i(0, 0, 100, 0xF)
        assertEquals(position.z, 100)
    }

    @Test
    fun `correct negative z`() {
        val position = SVec4i(0xF, 0xF, -4, 0xF)
        assertEquals(position.z, -4)
    }

    @Test
    fun `correct negative z large`() {
        val position = SVec4i(0, 0, -100, 0xF)
        assertEquals(position.z, -100)
    }

    @Test
    fun `correct plus z`() {
        val position = SVec4i(0xF, 0xF, 2, 0xF)
        assertEquals(position.plusZ().z, 3)
    }

    @Test
    fun `correct plus 2 z`() {
        val position = SVec4i(0xF, 0xF, 2, 0xF)
        assertEquals(position.plusZ(2).z, 4)
    }

    @Test
    fun `correct minus z`() {
        val position = SVec4i(0xF, 0xF, 2, 0xF)
        assertEquals(position.minusZ().z, 1)
    }

    @Test
    fun `correct positive w`() {
        val position = SVec4i(0xF, 0xF, 0xF, 4)
        assertEquals(position.w, 4)
    }

    @Test
    fun `correct positive w large`() {
        val position = SVec4i(0, 0, 0xF, 100)
        assertEquals(position.w, 100)
    }

    @Test
    fun `correct negative w`() {
        val position = SVec4i(0xF, 0xF, 0xF, -4)
        assertEquals(position.w, -4)
    }

    @Test
    fun `correct negative w large`() {
        val position = SVec4i(0, 0, 0xF, -100)
        assertEquals(position.w, -100)
    }

    @Test
    fun `correct plus w`() {
        val position = SVec4i(0xF, 0xF, 0xF, 2)
        assertEquals(position.plusW().w, 3)
    }

    @Test
    fun `correct plus 2 w`() {
        val position = SVec4i(0xF, 0xF, 0xF, 2)
        assertEquals(position.plusW(2).w, 4)
    }

    @Test
    fun `correct minus w`() {
        val position = SVec4i(0xF, 0xF, 0xF, 2)
        assertEquals(position.minusW().w, 1)
    }

    @Test
    fun `unary minus`() {
        val position = -SVec4i(2, 2, 2, 2)
        assertEquals(position.x, -2)
        assertEquals(position.y, -2)
        assertEquals(position.z, -2)
        assertEquals(position.w, -2)
    }

    @Test
    fun `unary plus`() {
        val position = +SVec4i(2, 2, 2, 2)
        assertEquals(position.x, 2)
        assertEquals(position.y, 2)
        assertEquals(position.z, 2)
        assertEquals(position.w, 2)
    }
}
