/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.system.base.texture

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.gui.atlas.TextureLikeTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureArray
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGLTextureUtil.readTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.util.KUtil.toResourceLocation

abstract class TextureManager {
    abstract val staticTextures: StaticTextureArray
    abstract val dynamicTextures: DynamicTextureArray

    lateinit var debugTexture: AbstractTexture
        private set
    lateinit var whiteTexture: TextureLikeTexture
        private set
    lateinit var steveTexture: DynamicTexture
        private set
    lateinit var alexTexture: DynamicTexture
        private set

    fun loadDefaultTextures(assetsManager: AssetsManager) {
        if (this::debugTexture.isInitialized) {
            throw IllegalStateException("Already initialized!")
        }
        debugTexture = staticTextures.createTexture(RenderConstants.DEBUG_TEXTURE_RESOURCE_LOCATION)
        whiteTexture = TextureLikeTexture(texture = staticTextures.createTexture(ResourceLocation("minosoft:textures/white.png")), uvStart = Vec2(0.0f, 0.0f), uvEnd = Vec2(0.001f, 0.001f), size = Vec2i(16, 16))

        loadDefaultSkins(assetsManager)
    }

    private fun loadDefaultSkins(assetsManager: AssetsManager) {
        // ToDo: For testing purposes only, they will be moved to static textures
        steveTexture = dynamicTextures.push("3780a46b-a725-4b22-8366-01056c698386".toUUID()) { assetsManager["minecraft:entity/steve".toResourceLocation().texture()].readTexture().second }
        alexTexture = dynamicTextures.push("3780a46b-a725-4b22-8366-01056c698386".toUUID()) { assetsManager["minecraft:entity/alex".toResourceLocation().texture()].readTexture().second }
    }
}
