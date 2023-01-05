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

package de.bixilon.minosoft.gui.rendering.system.base.texture.texture

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.system.base.texture.ShaderTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGLTextureUtil
import de.bixilon.minosoft.gui.rendering.textures.properties.ImageProperties
import java.nio.ByteBuffer

interface AbstractTexture : ShaderTexture {
    val resourceLocation: ResourceLocation

    var textureArrayUV: Vec2
    var atlasSize: Int
    var singlePixelSize: Vec2
    val state: TextureStates
    val size: Vec2i
    val transparency: TextureTransparencies
    var properties: ImageProperties

    var renderData: TextureRenderData

    var data: ByteBuffer?
    var mipmapData: Array<ByteBuffer>?
    var generateMipMaps: Boolean


    fun load(assetsManager: AssetsManager)

    override val shaderId: Int
        get() = renderData.shaderTextureId


    fun generateMipMaps(data: ByteBuffer = this.data!!): Array<ByteBuffer> {
        if (!generateMipMaps) {
            return arrayOf(data)
        }

        return OpenGLTextureUtil.generateMipMaps(data, size)
    }

    override fun transformUV(end: FloatArray?): FloatArray {
        return renderData.transformUV(end)
    }

    override fun transformUV(end: Vec2?): Vec2 {
        return renderData.transformUV(end)
    }

}
