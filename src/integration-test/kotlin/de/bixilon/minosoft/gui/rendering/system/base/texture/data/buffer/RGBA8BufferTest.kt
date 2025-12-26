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

package de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor.Companion.rgba
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.rgb
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["textures"])
class RGBA8BufferTest {

    fun `set rgba and check buffer at 0,0`() {
        val source = RGBA8Buffer(Vec2i(12, 13))
        source.setRGBA(0, 0, 0x11, 0x22, 0x33, 0x44)
        assertEquals(source.data.get(0), 0x11)
        assertEquals(source.data.get(1), 0x22)
        assertEquals(source.data.get(2), 0x33)
        assertEquals(source.data.get(3), 0x44)
    }

    fun `set rgba parts and check buffer at random`() {
        val source = RGBA8Buffer(Vec2i(12, 13))
        source.setRGBA(9, 3, 0x11, 0x22, 0x33, 0x44)
        assertEquals(source.data.get(180 + 0), 0x11)
        assertEquals(source.data.get(180 + 1), 0x22)
        assertEquals(source.data.get(180 + 2), 0x33)
        assertEquals(source.data.get(180 + 3), 0x44)
    }

    fun `set rgba and check buffer at random`() {
        val source = RGBA8Buffer(Vec2i(12, 13))
        source.setRGBA(9, 3, 0x11223344.rgba())
        assertEquals(source.data.get(180 + 0), 0x11)
        assertEquals(source.data.get(180 + 1), 0x22)
        assertEquals(source.data.get(180 + 2), 0x33)
        assertEquals(source.data.get(180 + 3), 0x44)
    }

    fun `get rgba at 0,0`() {
        val source = RGBA8Buffer(Vec2i(12, 13))
        source.data.put(0, 0x11).put(1, 0x22).put(2, 0x33).put(3, 0x44)
        val rgba = source.getRGBA(0, 0)
        assertEquals(rgba, 0x11223344.rgba())
        assertEquals(source.getR(0, 0), 0x11)
        assertEquals(source.getG(0, 0), 0x22)
        assertEquals(source.getB(0, 0), 0x33)
        assertEquals(source.getA(0, 0), 0x44)
    }

    fun `get rgba at 3,3`() {
        val source = RGBA8Buffer(Vec2i(12, 13))
        source.data.put(156 + 0, 0x11).put(156 + 1, 0x22).put(156 + 2, 0x33).put(156 + 3, 0x44)
        val rgba = source.getRGBA(3, 3)
        assertEquals(rgba, 0x11223344.rgba())
        assertEquals(source.getR(3, 3), 0x11)
        assertEquals(source.getG(3, 3), 0x22)
        assertEquals(source.getB(3, 3), 0x33)
        assertEquals(source.getA(3, 3), 0x44)
    }

    fun `get rgb at 3,3`() {
        val source = RGBA8Buffer(Vec2i(12, 13))
        source.data.put(156 + 0, 0x11).put(156 + 1, 0x22).put(156 + 2, 0x33).put(156 + 3, 0x44)
        val rgb = source.getRGB(3, 3)
        assertEquals(rgb, 0x112233.rgb())
    }


    fun `put complete texture`() {
        val source = RGBA8Buffer(Vec2i(12, 13))
        source.setRGBA(0, 0, 0x11, 0x22, 0x33, 0x44)
        source.setRGBA(10, 11, 0x11, 0x22, 0x33, 0x44)
        source.setRGBA(11, 12, 0x11, 0x22, 0x33, 0x44)

        val destination = RGBA8Buffer(Vec2i(12, 13))
        destination.put(source, Vec2i.EMPTY, Vec2i.EMPTY, Vec2i(12, 13))

        assertEquals(destination.getRGBA(0, 0), 0x11223344.rgba())
        assertEquals(destination.getRGBA(10, 11), 0x11223344.rgba())
        assertEquals(destination.getRGBA(11, 12), 0x11223344.rgba())
    }


    fun `put part of texture`() {
        val source = RGBA8Buffer(Vec2i(5, 4))
        source.setRGBA(1, 1, 0x11, 0x22, 0x33, 0x44)
        source.setRGBA(3, 3, 0x11, 0x22, 0x33, 0x44)

        val destination = RGBA8Buffer(Vec2i(11, 9))
        destination.put(source, Vec2i(1, 1), Vec2i(3, 3), Vec2i(4, 3))

        assertEquals(destination.getRGBA(3, 3), 0x11223344.rgba())
        assertEquals(destination.getRGBA(5, 5), 0x11223344.rgba())
    }

    fun `check transparency level opaque`() {
        val source = RGBA8Buffer(Vec2i(5, 4))
        source.fill(0xAA, 0xBB, 0xCC, 0xFF)
        source.setRGBA(1, 1, 0x11, 0x22, 0x33, 0xFF)

        assertEquals(source.getTransparency(), TextureTransparencies.OPAQUE)
    }

    fun `check transparency level transparent`() {
        val source = RGBA8Buffer(Vec2i(5, 4))
        source.fill(0xAA, 0xBB, 0xCC, 0xFF)
        source.setRGBA(1, 1, 0x11, 0x22, 0x33, 0x00)

        assertEquals(source.getTransparency(), TextureTransparencies.TRANSPARENT)
    }

    fun `check transparency level translucent`() {
        val source = RGBA8Buffer(Vec2i(5, 4))
        source.fill(0xAA, 0xBB, 0xCC, 0xFF)
        source.setRGBA(1, 1, 0x11, 0x22, 0x33, 0x01)

        assertEquals(source.getTransparency(), TextureTransparencies.TRANSLUCENT)
    }

    fun `check transparency level transparent translucent`() {
        val source = RGBA8Buffer(Vec2i(5, 4))
        source.fill(0xAA, 0xBB, 0xCC, 0xFF)
        source.setRGBA(1, 1, 0x11, 0x22, 0x33, 0x00)
        source.setRGBA(1, 2, 0x11, 0x22, 0x33, 0x01)

        assertEquals(source.getTransparency(), TextureTransparencies.TRANSLUCENT)
    }

    fun `interpolate 50`() {
        val a = RGB8Buffer(Vec2i(2, 2))
        a.setRGB(0, 0, RGBColor(255, 0, 0))

        val b = RGB8Buffer(Vec2i(2, 2))
        b.setRGB(0, 0, RGBColor(0, 255, 0))

        val destination = RGB8Buffer(Vec2i(2, 2))

        destination.interpolate(a, b, 0.5f)

        assertEquals(destination.getRGB(0, 0), RGBColor(127, 127, 0))
    }
}
