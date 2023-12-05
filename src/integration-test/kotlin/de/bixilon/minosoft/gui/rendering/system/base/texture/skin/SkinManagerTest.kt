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

package de.bixilon.minosoft.gui.rendering.system.base.texture.skin

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.TextureData
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.readTexture
import de.bixilon.minosoft.test.IT
import org.testng.Assert.*
import org.testng.annotations.Test

@Test(groups = ["rendering", "textures"], enabled = false) // TODO: flip skin correctly
class SkinManagerTest {
    val skin = IT.OBJENESIS.newInstance(SkinManager::class.java)
    val readSkin = SkinManager::class.java.getDeclaredMethod("readSkin", ByteArray::class.java).apply { isAccessible = true }
    val isReallyWide = SkinManager::class.java.getDeclaredMethod("isReallyWide", TextureBuffer::class.java).apply { isAccessible = true }


    private fun ByteArray.readSkin(): TextureData {
        return readSkin.invoke(skin, this) as TextureData
    }

    fun `automatically detect and fix legacy skin`() {
        val old = SkinManager::class.java.getResourceAsStream("/skins/7af7c07d1ded61b1d3312685b32e4568ffdda762ec8d808895cc329a93d606e0.png")!!.readAllBytes().readSkin()
        val expected = SkinManager::class.java.getResourceAsStream("/skins/7af7c07d1ded61b1d3312685b32e4568ffdda762ec8d808895cc329a93d606e0_fixed.png")!!.readTexture()

        assertEquals(old.size, Vec2i(64, 64)) // fixed size
        assertEquals(expected.size, Vec2i(64, 64))

        old.buffer.data.rewind()
        expected.data.rewind()

        // TextureUtil.dump(File("/home/moritz/test.png"), old.size, old.buffer, true, false)
        assertEquals(old.buffer.data, expected.data)
    }

    fun `check if skin is really wide on a slim skin`() {
        val slim = SkinManager::class.java.getResourceAsStream("/skins/5065405b55a729be5a442832b895d4352b3fdcc61c8c57f4b8abad64344194d3.png")!!.readAllBytes().readSkin()
        assertFalse(isReallyWide.invoke(SkinManager, slim) as Boolean)
    }

    fun `check if skin is really wide on a wide skin`() {
        val slim = SkinManager::class.java.getResourceAsStream("/skins/182f56f61cb8ec6e8938a5b7b515d209be55bb91e9969ba7bf9521293834cda2.png")!!.readAllBytes().readSkin()
        assertTrue(isReallyWide.invoke(SkinManager, slim) as Boolean)
    }
}
