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

import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.rgb
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RGBColorTest {

    @Test
    fun `color int components`() {
        val color = RGBColor(0x12, 0x34, 0x56)
        assertEquals(color.red, 0x12)
        assertEquals(color.green, 0x34)
        assertEquals(color.blue, 0x56)
    }

    @Test
    fun `color float components`() {
        val color = RGBColor(0x12, 0x34, 0x56)
        assertEquals(color.redf, 0x12 / 255.0f, 0.01f)
        assertEquals(color.greenf, 0x34 / 255.0f, 0.01f)
        assertEquals(color.bluef, 0x56 / 255.0f, 0.01f)
    }

    @Test
    fun `color swizzles`() {
        val color = RGBColor(0x12, 0x34, 0x56)
        assertEquals(color.rgb, 0x123456)
    }

    @Test
    fun `out of bounds clamping positive`() {
        val color = RGBColor(0x145, 0x145, 0x145)
        assertEquals(color.red, 0xFF)
        assertEquals(color.green, 0xFF)
        assertEquals(color.blue, 0xFF)
    }

    @Test
    fun `out of bounds clamping negative`() {
        val color = RGBColor(-123, -123, -123)
        assertEquals(color.red, 0)
        assertEquals(color.green, 0)
        assertEquals(color.blue, 0)
    }

    @Test
    fun `operations minus`() {
        val a = RGBColor(0x12, 0x34, 0x56)
        val b = RGBColor(0x05, 0x06, 0x07)

        assertEquals(a - b, RGBColor(0x0D, 0x2e, 0x4F))
        assertEquals(a - 3, RGBColor(0x0F, 0x31, 0x53))
        assertEquals(a - 0.1f, RGBColor(0x00, 0x1b, 0x3d))
    }

    @Test
    fun `conversion rgba`() {
        val color = RGBColor(0x12, 0x34, 0x56)

        assertEquals(color.rgba(), RGBAColor(0x12, 0x34, 0x56, 0xFF))
    }

    @Test
    fun `int to rgb`() {
        assertEquals(0x123456.rgb(), RGBColor(0x12, 0x34, 0x56))
    }

    @Test
    fun `string to rgb`() {
        assertEquals("#123456".rgb(), RGBColor(0x12, 0x34, 0x56))
        assertEquals("#12345678".rgb(), RGBColor(0x12, 0x34, 0x56))
    }


    @Test
    fun `multiply white`() {
        val a = ChatColors.WHITE.rgb()
        val b = ChatColors.WHITE.rgb()

        assertEquals(a * b, ChatColors.WHITE.rgb())
    }

    @Test
    fun `multiply black`() {
        val a = ChatColors.BLACK.rgb()
        val b = ChatColors.BLACK.rgb()

        assertEquals(a * b, ChatColors.BLACK.rgb())
    }

    @Test
    fun `multiply grey`() {
        val grey = RGBColor(0.5f, 0.5f, 0.5f).rgb()

        assertEquals(grey * grey, RGBColor(0.25f, 0.25f, 0.25f))
    }


    // TODO: operations (plus, times), toString, Int::rgb, Int::rgba, mix
}
