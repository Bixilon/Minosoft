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
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.readTexture
import de.bixilon.minosoft.test.IT
import org.testng.Assert.assertEquals
import org.testng.annotations.Test

@Test(groups = ["rendering", "textures"], enabled = false) // TODO: flip skin correctly
class SkinManagerTest {
    val skin = IT.OBJENESIS.newInstance(SkinManager::class.java)
    val readSkin = SkinManager::class.java.getDeclaredMethod("readSkin", ByteArray::class.java).apply { isAccessible = true }


    private fun ByteArray.readSkin(): TextureData {
        return readSkin.invoke(skin, this) as TextureData
    }

    fun `automatically detect and fix legacy skin`() {
        val old = SkinManager::class.java.getResourceAsStream("/skins/7af7c07d1ded61b1d3312685b32e4568ffdda762ec8d808895cc329a93d606e0.png")!!.readAllBytes().readSkin()
        val expected = SkinManager::class.java.getResourceAsStream("/skins/7af7c07d1ded61b1d3312685b32e4568ffdda762ec8d808895cc329a93d606e0_fixed.png")!!.readTexture()

        assertEquals(old.size, Vec2i(64, 64)) // fixed size
        assertEquals(expected.size, Vec2i(64, 64))

        old.buffer.rewind()
        expected.buffer.rewind()

        // TextureUtil.dump(File("/home/moritz/test.png"), old.size, old.buffer, true, false)
        assertEquals(old.buffer, expected.buffer)
    }

}
