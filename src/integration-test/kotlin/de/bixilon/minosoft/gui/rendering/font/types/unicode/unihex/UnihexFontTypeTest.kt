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

package de.bixilon.minosoft.gui.rendering.font.types.unicode.unihex

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.kutil.buffer.ByteBufferUtil.readRemaining
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
import de.bixilon.kutil.unsafe.UnsafeUtil.setUnsafeAccessible
import de.bixilon.minosoft.gui.rendering.font.types.unicode.UnicodeCodeRenderer
import de.bixilon.minosoft.test.ITUtil.allocate
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.testng.Assert.assertEquals
import org.testng.Assert.assertTrue
import org.testng.annotations.Test
import java.io.ByteArrayInputStream
import java.io.InputStream
import kotlin.reflect.full.companionObject

@Test(groups = ["font"])
class UnihexFontTypeTest {
    private val textureRemaining = UnifontTexture::class.java.getFieldOrNull("remaining")!!
    val readUnihex = UnihexFontType::class.companionObject!!.java.getDeclaredMethod("readUnihex", InputStream::class.java, Int2ObjectOpenHashMap::class.java).apply { setUnsafeAccessible() }

    fun `read unihex`() {
        val data = "0E01DB:555580000001A2382241A238140588380001B31808A1913020A9BB100001AAAA\n298B:0000000E0808080808080808080E000E"

        val chars = Int2ObjectOpenHashMap<ByteArray>()
        readUnihex(UnihexFontType, ByteArrayInputStream(data.toByteArray()), chars)

        assertEquals(chars.size, 2)
        val char = chars['\u298B'.code]
        assertEquals(char.size, 16)
        assertEquals(char, byteArrayOf(0x00, 0x00, 0x00, 0x0E, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x0E, 0x00, 0x0E))
    }

    fun `empty read unihex`() {
        val data = ""

        val chars = Int2ObjectOpenHashMap<ByteArray>()
        readUnihex(UnihexFontType, ByteArrayInputStream(data.toByteArray()), chars)

        assertEquals(chars.size, 0)
    }

    fun `trailing new lines read unihex`() {
        val data = "\n0E01DB:555580000001A2382241A238140588380001B31808A1913020A9BB100001AAAA\n298B:0000000E0808080808080808080E000E\n\n"

        val chars = Int2ObjectOpenHashMap<ByteArray>()
        readUnihex(UnihexFontType, ByteArrayInputStream(data.toByteArray()), chars)

        assertEquals(chars.size, 2)
        val char = chars['\u298B'.code]
        assertEquals(char.size, 16)
        assertEquals(char, byteArrayOf(0x00, 0x00, 0x00, 0x0E, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x0E, 0x00, 0x0E))
    }

    private fun ByteArray.assertPixel(index: Int, set: Boolean = true) {
        val offset = index * 4

        val value = this[offset + 0].toInt() or this[offset + 1].toInt() or this[offset + 2].toInt() or this[offset + 3].toInt()

        if (set) {
            assertTrue(value != 0, "Did expect pixel at $index")
        } else {
            assertTrue(value == 0, "Did not expect pixel at $index")
        }
    }

    fun `basic rasterizing`() {
        val pixels = byteArrayOf(0x00, 0x00, 0x00, 0x0E, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x0E, 0x00, 0x0E)

        val rasterizer = UnifontRasterizer::class.java.allocate()

        val texture = UnifontTexture(1)
        assertEquals(texture.size, Vec2i(16, 16))
        val remaining = textureRemaining.get(texture) as IntArray
        assertEquals(remaining, intArrayOf(16))

        val textures = ArrayList<UnifontTexture>()
        textures += texture
        rasterizer::class.java.getFieldOrNull("textures")!!.set(rasterizer, textures)

        val code = rasterizer.add(pixels) as UnicodeCodeRenderer

        assertEquals(code.width, 1.5f)
        assertEquals(code.uvStart, Vec2f(0.0f, 0.0f))
        assertEquals(code.uvEnd, Vec2f(0.18749f, 1.0f))

        assertEquals(remaining, intArrayOf(13))
        val data = texture.data.buffer.data.readRemaining()


        for (index in 0 until 48) {
            data.assertPixel(index, false)
        }

        data.assertPixel(48)
        data.assertPixel(49)
        data.assertPixel(50)
        data.assertPixel(51, false)
    }


    // TODO: texture creation, rasterization, size override
}
