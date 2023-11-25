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

package de.bixilon.minosoft.gui.rendering.textures

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBufferFactory
import org.testng.Assert.assertEquals
import org.testng.annotations.Test
import java.io.ByteArrayInputStream
import java.io.InputStream

@Test(groups = ["texture", "assets"])
class TextureReadingTest {
    private val GRAY_GRAY = TextureReadingTest::class.java.getResourceAsStream("/texture_reading/gray_gray.png")!!.readAllBytes()
    private val GRAY_RGB = TextureReadingTest::class.java.getResourceAsStream("/texture_reading/gray_rgb.png")!!.readAllBytes()

    private val READ_1 = TextureUtil::class.java.getDeclaredMethod("readTexture1", InputStream::class.java, TextureBufferFactory::class.java).apply { isAccessible = true }
    private val READ_2 = TextureUtil::class.java.getDeclaredMethod("readTexture2", InputStream::class.java, TextureBufferFactory::class.java).apply { isAccessible = true }

    private fun TextureBuffer.assertGray() {
        assertEquals(size, Vec2i(16, 16))

        assertEquals(getR(0, 0), 0x94)
        assertEquals(getG(0, 0), 0x94)
        assertEquals(getB(0, 0), 0x94)
        assertEquals(getA(0, 0), 0xFF)

        assertEquals(getR(1, 0), 0xC3)
        assertEquals(getR(0, 2), 0xA3)
        assertEquals(getR(0, 4), 0xA2)
    }

    fun `read rgb 1`() {
        val texture = READ_1.invoke(TextureUtil, ByteArrayInputStream(GRAY_RGB), null) as TextureBuffer
        texture.assertGray()
    }

    fun `read rgb 2`() {
        val texture = READ_2.invoke(TextureUtil, ByteArrayInputStream(GRAY_RGB), null) as TextureBuffer
        texture.assertGray()
    }

    @Test(enabled = false)
    fun `read gray 1`() {
        val texture = READ_1.invoke(TextureUtil, ByteArrayInputStream(GRAY_GRAY), null) as TextureBuffer
        texture.assertGray()
    }

    fun `read gray 2`() {
        val texture = READ_2.invoke(TextureUtil, ByteArrayInputStream(GRAY_GRAY), null) as TextureBuffer
        texture.assertGray()
    }
}
