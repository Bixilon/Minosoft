/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY_INSTANCE
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["textures"])
class RGB8BufferTest {

    fun `set rgb and check buffer at 0,0`() {
        val source = RGB8Buffer(Vec2i(12, 13))
        source.setRGBA(0, 0, 0x11, 0x22, 0x33, 0x00)
        assertEquals(source.data.get(0), 0x11)
        assertEquals(source.data.get(1), 0x22)
        assertEquals(source.data.get(2), 0x33)
    }

    fun `set rgba parts and check buffer at random`() {
        val source = RGB8Buffer(Vec2i(12, 13))
        source.setRGBA(9, 3, 0x11, 0x22, 0x33, 0x00)
        assertEquals(source.data.get(135 + 0), 0x11)
        assertEquals(source.data.get(135 + 1), 0x22)
        assertEquals(source.data.get(135 + 2), 0x33)
    }

    fun `set rgba and check buffer at random`() {
        val source = RGB8Buffer(Vec2i(12, 13))
        source.setRGBA(9, 3, 0x112233FF)
        assertEquals(source.data.get(135 + 0), 0x11)
        assertEquals(source.data.get(135 + 1), 0x22)
        assertEquals(source.data.get(135 + 2), 0x33)
    }

    fun `get rgba at 0,0`() {
        val source = RGB8Buffer(Vec2i(12, 13))
        source.data.put(0, 0x11).put(1, 0x22).put(2, 0x33).put(3, 0x44)
        val rgba = source.getRGBA(0, 0)
        assertEquals(rgba, 0x112233FF)
        assertEquals(source.getR(0, 0), 0x11)
        assertEquals(source.getG(0, 0), 0x22)
        assertEquals(source.getB(0, 0), 0x33)
        assertEquals(source.getA(0, 0), 0xFF)
    }

    fun `get rgba at 3,3`() {
        val source = RGB8Buffer(Vec2i(12, 13))
        source.data.put(117 + 0, 0x11).put(117 + 1, 0x22).put(117 + 2, 0x33)
        val rgba = source.getRGBA(3, 3)
        assertEquals(rgba, 0x112233FF)
        assertEquals(source.getR(3, 3), 0x11)
        assertEquals(source.getG(3, 3), 0x22)
        assertEquals(source.getB(3, 3), 0x33)
        assertEquals(source.getA(3, 3), 0xFF)
    }


    fun `put complete texture`() {
        val source = RGB8Buffer(Vec2i(12, 13))
        source.setRGBA(0, 0, 0x11, 0x22, 0x33, 0x00)
        source.setRGBA(10, 11, 0x11, 0x22, 0x33, 0x00)
        source.setRGBA(11, 12, 0x11, 0x22, 0x33, 0x00)

        val destination = RGB8Buffer(Vec2i(12, 13))
        destination.put(source, Vec2i.EMPTY_INSTANCE, Vec2i.EMPTY_INSTANCE, Vec2i(12, 13))

        assertEquals(destination.getRGBA(0, 0), 0x112233FF)
        assertEquals(destination.getRGBA(10, 11), 0x112233FF)
        assertEquals(destination.getRGBA(11, 12), 0x112233FF)
    }


    fun `put part of texture`() {
        val source = RGB8Buffer(Vec2i(5, 4))
        source.setRGBA(1, 1, 0x11, 0x22, 0x33, 0x00)
        source.setRGBA(3, 3, 0x11, 0x22, 0x33, 0x00)

        val destination = RGB8Buffer(Vec2i(11, 9))
        destination.put(source, Vec2i(1, 1), Vec2i(3, 3), Vec2i(4, 3))

        assertEquals(destination.getRGBA(3, 3), 0x112233FF)
        assertEquals(destination.getRGBA(5, 5), 0x112233FF)
    }
}
