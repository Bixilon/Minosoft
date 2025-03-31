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

package de.bixilon.minosoft.data.text.formatting.color

import de.bixilon.minosoft.data.text.formatting.color.RGBAColor.Companion.rgba
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RGBAColorTest {

    @Test
    fun `color int components`() {
        val color = RGBAColor(0x12, 0x34, 0x56, 0x78)
        assertEquals(color.red, 0x12)
        assertEquals(color.green, 0x34)
        assertEquals(color.blue, 0x56)
        assertEquals(color.alpha, 0x78)
    }

    @Test
    fun `color float components`() {
        val color = RGBAColor(0x12, 0x34, 0x56, 0x78)
        assertEquals(color.redf, 0x12 / 255.0f, 0.01f)
        assertEquals(color.greenf, 0x34 / 255.0f, 0.01f)
        assertEquals(color.bluef, 0x56 / 255.0f, 0.01f)
        assertEquals(color.alphaf, 0x78 / 255.0f, 0.01f)
    }

    @Test
    fun `color swizzles`() {
        val color = RGBAColor(0x12, 0x34, 0x56, 0x78)
        assertEquals(color.rgba, 0x12345678)
        assertEquals(color.rgb, 0x123456)
        assertEquals(color.argb, 0x78123456)
    }

    @Test
    fun `default alpha`() {
        val color = RGBAColor(0x12, 0x34, 0x56)
        assertEquals(color.alpha, 0xFF)
    }

    @Test
    fun `out of bounds clamping positive`() {
        val color = RGBAColor(0x145, 0x145, 0x145, 0x145)
        assertEquals(color.red, 0xFF)
        assertEquals(color.green, 0xFF)
        assertEquals(color.blue, 0xFF)
        assertEquals(color.alpha, 0xFF)
    }

    @Test
    fun `out of bounds clamping negative`() {
        val color = RGBAColor(-123, -123, -123, -123)
        assertEquals(color.red, 0)
        assertEquals(color.green, 0)
        assertEquals(color.blue, 0)
        assertEquals(color.alpha, 0)
    }

    @Test
    fun `operations minus`() {
        val a = RGBAColor(0x12, 0x34, 0x56, 0x78)
        val b = RGBAColor(0x05, 0x06, 0x07, 0x08)

        assertEquals(a - b, RGBAColor(0x0D, 0x2e, 0x4F, 0x70))
        assertEquals(a - 3, RGBAColor(0x0F, 0x31, 0x53, 0x75))
        assertEquals(a - 0.1f, RGBAColor(0x00, 0x1b, 0x3d, 0x5f))
    }

    @Test
    fun `conversion rgb`() {
        val color = RGBAColor(0x12, 0x34, 0x56, 0x78)

        assertEquals(color.rgb(), RGBColor(0x12, 0x34, 0x56))
    }

    @Test
    fun `int to rgba`() {
        assertEquals(0x12345678.rgba(), RGBAColor(0x12, 0x34, 0x56, 0x78))
    }

    @Test
    fun `string to rgba`() {
        assertEquals("#123456".rgba(), RGBAColor(0x12, 0x34, 0x56))
        assertEquals("#12345678".rgba(), RGBAColor(0x12, 0x34, 0x56, 0x78))
    }

    // TODO: operations (plus, times), toString, mix
}
