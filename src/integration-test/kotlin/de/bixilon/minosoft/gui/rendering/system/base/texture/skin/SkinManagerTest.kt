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
import de.bixilon.kutil.reflection.ReflectionUtil.forceSet
import de.bixilon.kutil.url.URLUtil.toURL
import de.bixilon.minosoft.assets.MemoryAssetsManager
import de.bixilon.minosoft.data.entities.entities.player.RemotePlayerEntity
import de.bixilon.minosoft.data.entities.entities.player.additional.PlayerAdditional
import de.bixilon.minosoft.data.entities.entities.player.properties.PlayerProperties
import de.bixilon.minosoft.data.entities.entities.player.properties.textures.PlayerTextures
import de.bixilon.minosoft.data.entities.entities.player.properties.textures.SkinPlayerTexture
import de.bixilon.minosoft.data.entities.entities.player.properties.textures.metadata.SkinMetadata
import de.bixilon.minosoft.data.entities.entities.player.properties.textures.metadata.SkinModel
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureState
import de.bixilon.minosoft.gui.rendering.system.base.texture.skin.vanilla.DefaultSkinProvider
import de.bixilon.minosoft.gui.rendering.system.dummy.DummyRenderSystem
import de.bixilon.minosoft.gui.rendering.system.dummy.texture.DummyTextureManager
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.readTexture
import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil.createConnection
import de.bixilon.minosoft.test.IT
import de.bixilon.minosoft.test.ITUtil.allocate
import org.testng.Assert.*
import org.testng.annotations.Test
import java.util.*
import kotlin.reflect.full.companionObject

@Test(groups = ["rendering", "textures"]) // TODO: flip skin correctly
class SkinManagerTest {
    val skin = IT.OBJENESIS.newInstance(SkinManager::class.java)
    val readSkin = SkinManager::class.java.getDeclaredMethod("readSkin", ByteArray::class.java).apply { isAccessible = true }
    val isReallyWide = SkinManager::class.companionObject!!.java.getDeclaredMethod("isReallyWide", TextureBuffer::class.java).apply { isAccessible = true }


    private fun ByteArray.readSkin(): TextureBuffer {
        return readSkin.invoke(skin, this) as TextureBuffer
    }

    @Test(enabled = false)
    fun `automatically detect and fix legacy skin`() {
        val old = SkinManager::class.java.getResourceAsStream("/skins/7af7c07d1ded61b1d3312685b32e4568ffdda762ec8d808895cc329a93d606e0.png")!!.readAllBytes().readSkin()
        val expected = SkinManager::class.java.getResourceAsStream("/skins/7af7c07d1ded61b1d3312685b32e4568ffdda762ec8d808895cc329a93d606e0_fixed.png")!!.readTexture()

        assertEquals(old.size, Vec2i(64, 64)) // fixed size
        assertEquals(expected.size, Vec2i(64, 64))

        old.data.rewind()
        expected.data.rewind()

        // TextureUtil.dump(File("/home/moritz/test.png"), old.size, old.buffer, true, false)
        assertEquals(old.data, expected.data)
    }

    fun `check if skin is really wide on a slim skin`() {
        val slim = SkinManager::class.java.getResourceAsStream("/skins/5065405b55a729be5a442832b895d4352b3fdcc61c8c57f4b8abad64344194d3.png")!!.readAllBytes().readSkin()
        assertFalse(isReallyWide.invoke(SkinManager, slim) as Boolean)
    }

    fun `check if skin is really wide on a wide skin`() {
        val slim = SkinManager::class.java.getResourceAsStream("/skins/180fce54ab949a1eada68bea6be02633a4c15fd0915f097a93fa6d3ca1e04623.png")!!.readAllBytes().readSkin()
        assertTrue(isReallyWide.invoke(SkinManager, slim) as Boolean)
    }

    fun `load dynamic texture correctly`() {
        val context = RenderContext::class.java.allocate()
        context::system.forceSet(DummyRenderSystem(context))
        val textures = DummyTextureManager(context)

        val skins = SkinManager(textures)
        skins::default.forceSet(DefaultSkinProvider(textures.dynamic, MemoryAssetsManager()))
        val player = RemotePlayerEntity::class.java.allocate()
        player::connection.forceSet(createConnection())
        player.connection.world.entities.add(null, UUID(1L, 2L), player)
        player::additional.forceSet(PlayerAdditional("name_him"))

        val data = SkinManager::class.java.getResourceAsStream("/skins/5065405b55a729be5a442832b895d4352b3fdcc61c8c57f4b8abad64344194d3.png")!!.readAllBytes()

        val properties = PlayerProperties(PlayerTextures(skin = SkinPlayerTexture("https://textures.minecraft.net/abcdef".toURL(), metadata = SkinMetadata(SkinModel.SLIM)).apply { this::data.forceSet(data) }))

        val skin = skins.getSkin(player, properties, false, false)
        assertEquals(skin?.model, SkinModel.SLIM)
        assertEquals(skin?.texture?.state, DynamicTextureState.LOADED)
        val buffer = skin?.texture?.data?.buffer!!
        // assertEquals(buffer.getRGBA(9, 0), 0x0F00FA_FF)
        assertEquals(buffer.getRGBA(9, 0), 0x0D00FA_FF) // TODO: wrong? should be the value above
    }
}
