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

package de.bixilon.minosoft.gui.rendering.gui.atlas.textures

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.RGBA8Buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.test.IT
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["atlas", "gui"])
class AtlasTextureManagerTest {

    private fun create(): AtlasTextureManager {
        val context = IT.OBJENESIS.newInstance(RenderContext::class.java)

        return AtlasTextureManager(context)
    }

    fun construct() {
        create()
    }

    private fun createTexture(size: Vec2i): TextureBuffer {
        val buffer = RGBA8Buffer(size)
        for (index in 0 until buffer.data.limit()) {
            buffer.data.put(0xFF.toByte())
        }

        for (y in 0 until size.y) {
            for (x in 0 until size.x) {
                val offset = ((size.x * y) + x) * 4
                buffer.data.put(offset, (x + 1).toByte())
            }
        }

        return buffer
    }

    private fun AtlasTexture.getR(x: Int, y: Int): Int {
        return data.buffer.getR(x, y)
    }

    fun `copy simple texture to atlas texture`() {
        val texture = AtlasTexture(Vec2i(16, 16))
        val source = createTexture(Vec2i(4, 4))
        texture.put(Vec2i(1, 1), source, Vec2i(1, 1), Vec2i(3, 3))

        assertEquals(texture.getR(0, 0), 0x00)
        assertEquals(texture.getR(1, 0), 0x00)
        assertEquals(texture.getR(2, 0), 0x00)
        assertEquals(texture.getR(3, 0), 0x00)
        assertEquals(texture.getR(4, 0), 0x00)

        assertEquals(texture.getR(0, 1), 0x00)
        assertEquals(texture.getR(1, 1), 0x02)
        assertEquals(texture.getR(2, 1), 0x03)
        assertEquals(texture.getR(3, 1), 0x04)
        assertEquals(texture.getR(4, 1), 0x00)

        assertEquals(texture.getR(0, 2), 0x00)
        assertEquals(texture.getR(1, 2), 0x02)
        assertEquals(texture.getR(2, 2), 0x03)
        assertEquals(texture.getR(3, 2), 0x04)
        assertEquals(texture.getR(4, 2), 0x00)

        assertEquals(texture.getR(0, 3), 0x00)
        assertEquals(texture.getR(1, 3), 0x02)
        assertEquals(texture.getR(2, 3), 0x03)
        assertEquals(texture.getR(3, 3), 0x04)
        assertEquals(texture.getR(4, 3), 0x00)

        assertEquals(texture.getR(0, 4), 0x00)
        assertEquals(texture.getR(1, 4), 0x00)
        assertEquals(texture.getR(2, 4), 0x00)
        assertEquals(texture.getR(3, 4), 0x00)
        assertEquals(texture.getR(4, 4), 0x00)
    }
}
