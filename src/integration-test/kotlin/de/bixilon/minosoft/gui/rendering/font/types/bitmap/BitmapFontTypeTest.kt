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

package de.bixilon.minosoft.gui.rendering.font.types.bitmap

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.font.types.empty.EmptyCodeRenderer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.TextureData
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.RGBA8Buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.system.dummy.texture.DummyTexture
import org.testng.Assert.*
import org.testng.annotations.Test
import java.util.stream.IntStream
import kotlin.reflect.full.companionObject


@Test(groups = ["font"])
class BitmapFontTypeTest {
    private val LOAD = BitmapFontType::class.companionObject!!.java.getDeclaredMethod("load", Texture::class.java, Int::class.java, Int::class.java, Array<IntStream>::class.java).apply { isAccessible = true }

    private fun createTexture(start: IntArray, end: IntArray, width: Int, height: Int, rows: Int): Texture {
        check(start.size == end.size)
        val size = Vec2i(width * 16, rows * height)

        val buffer = RGBA8Buffer(size)

        for (row in 0 until rows) {
            for (height in 0 until height) {
                for (char in 0 until 16) {
                    val start = start.getOrNull((row * 16) + char) ?: 0
                    val end = end.getOrNull((row * 16) + char) ?: width

                    for (pixel in 0 until width) {
                        buffer.data.put(0xFF.toByte())
                        buffer.data.put(0xFF.toByte())
                        buffer.data.put(0xFF.toByte())

                        if (pixel in start..end) {
                            buffer.data.put(0xFF.toByte())
                        } else {
                            buffer.data.put(0x00.toByte())
                        }
                    }
                }
            }
        }
        assertEquals(buffer.data.position(), buffer.data.limit())

        val texture = DummyTexture()
        texture.size = size
        texture.data = TextureData(buffer)

        return texture
    }

    private fun load(start: IntArray, end: IntArray, width: Int = 8, height: Int = 8, ascent: Int = 7, chars: Array<IntArray>): BitmapFontType {
        val texture = createTexture(start, end, width, height, chars.size)

        val fontType = LOAD(BitmapFontType, texture, height, ascent, chars.map { IntStream.of(*it) }.toTypedArray()) as BitmapFontType


        return fontType
    }

    private fun BitmapFontType.assert(char: Char, width: Int, uvStart: Vec2, uvEnd: Vec2, height: Int? = null) {
        val char = this[char.code]
        assertNotNull(char)
        char as BitmapCodeRenderer
        assertEquals(char.width, width.toFloat(), "width mismatch")
        assertEquals(char.uvStart, uvStart, "uv start mismatch")
        assertEquals(char.uvEnd, uvEnd, "uv end mismatch")
        height?.let { assertEquals(char.height, height.toFloat()) }
    }

    fun `space size`() {
        val font = load(intArrayOf(-1), intArrayOf(-1), chars = arrayOf(intArrayOf('a'.code)))

        val a = font['a'.code]
        assertTrue(a is EmptyCodeRenderer)
        assertEquals((a as EmptyCodeRenderer).width, 4)
    }

    fun `load basic with default options`() {
        val font = load(intArrayOf(1, 2, 3), intArrayOf(7, 4, 6), chars = arrayOf(intArrayOf('a'.code, 'b'.code, 'c'.code)))

        font.assert('a', 7, Vec2(0.0078225f, 0), Vec2(0.0546775f, 1.0f))
        font.assert('b', 3, Vec2(0.078135f, 0), Vec2(0.0859275f, 1.0f))
        font.assert('c', 4, Vec2(0.1484475f, 0.0), Vec2(0.15624f, 1.0f))
    }

    fun `multiple rows`() {
        val font = load(IntArray(64) { it % 3 }, IntArray(64) { (it + 2) % 4 }, chars = arrayOf(IntArray(16) { 'A'.code + it }, IntArray(16) { 'A'.code + 16 + it }, IntArray(16) { 'A'.code + 32 + it }, IntArray(16) { 'A'.code + 48 + it }))

        font.assert('A', 3, Vec2(0.0f, 0), Vec2(0.0234275f, 0.24999f))
        font.assert('P', 2, Vec2(0.93751f, 0.0), Vec2(0.953115f, 0.24999f))

        font.assert('Q', 2, Vec2(0.0078225f, 0.25001f), Vec2(0.015615f, 0.49999f))
        font.assert('a', 1, Vec2(0.015635f, 0.50001f), Vec2(0.0078025f, 0.74999f))
        font.assert('q', 3, Vec2(0.0f, 0.75001f), Vec2(0.0234275f, 1.0f))
    }

    fun `12 px height`() {
        val font = load(intArrayOf(1, 2), intArrayOf(6, 7), width = 8, height = 12, ascent = 10, arrayOf(intArrayOf('ä'.code, 'ö'.code), intArrayOf(), intArrayOf()))

        font.assert('ä', 6, Vec2(0.0078225f, 0.0f), Vec2(0.046865f, 0.33332333f), height = 12)
    }
}
