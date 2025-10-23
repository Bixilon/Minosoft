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

package de.bixilon.minosoft.gui.rendering.system.base.texture

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.gui.atlas.textures.CodeTexturePart
import de.bixilon.minosoft.gui.rendering.shader.types.TextureShader
import de.bixilon.minosoft.gui.rendering.system.base.shader.ShaderUniforms
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.StaticTextureArray
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.font.FontTextureArray
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureArray
import de.bixilon.minosoft.gui.rendering.system.base.texture.skin.SkinManager
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.texture
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

abstract class TextureManager {
    abstract val static: StaticTextureArray
    abstract val dynamic: DynamicTextureArray
    abstract val font: FontTextureArray

    lateinit var debugTexture: Texture
        private set
    lateinit var whiteTexture: CodeTexturePart
        private set
    lateinit var skins: SkinManager
        private set

    fun loadDefaultTextures() {
        if (this::debugTexture.isInitialized) {
            throw IllegalStateException("Already initialized!")
        }
        debugTexture = static.create(RenderConstants.DEBUG_TEXTURE_RESOURCE_LOCATION)
        whiteTexture = CodeTexturePart(texture = static.create(minosoft("white").texture(), mipmaps = false), uvStart = Vec2f(0.0f, 0.0f), uvEnd = Vec2f(0.001f, 0.001f), size = Vec2i(16, 16))
    }

    fun initializeSkins(session: PlaySession) {
        skins = SkinManager(this)
        skins.initialize(session.account, session.assetsManager)
    }

    fun use(shader: TextureShader, name: String = ShaderUniforms.TEXTURES) {
        static.use(shader, name)
        dynamic.use(shader, name)
        font.use(shader, name)
    }

    fun reload() {
        dynamic.reload()
    }
}
