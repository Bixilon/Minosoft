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

package de.bixilon.minosoft.gui.rendering.system.base.texture.texture

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureTransparencies
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGLTextureUtil.readTexture
import de.bixilon.minosoft.gui.rendering.textures.properties.ImageProperties
import java.nio.ByteBuffer


class PNGTexture(
    override val resourceLocation: ResourceLocation,
    override var generateMipMaps: Boolean = true,
) : AbstractTexture {
    override lateinit var renderData: TextureRenderData

    override lateinit var textureArrayUV: Vec2
    override lateinit var singlePixelSize: Vec2
    override var atlasSize: Int = -1
    override var state: TextureStates = TextureStates.DECLARED
        private set
    override lateinit var size: Vec2i
        private set
    override lateinit var transparency: TextureTransparencies
        private set
    override lateinit var properties: ImageProperties


    override var data: ByteBuffer? = null
    override var mipmapData: Array<ByteBuffer>? = null


    @Synchronized
    override fun load(assetsManager: AssetsManager) {
        if (state == TextureStates.LOADED) {
            return
        }

        val (size, data) = assetsManager[resourceLocation].readTexture()

        this.size = size
        transparency = TextureTransparencies.OPAQUE
        for (i in 0 until data.limit() / 4) {
            val alpha = data[i * 4 + 3].toInt() and 0xFF
            if (alpha == 0x00) {
                transparency = TextureTransparencies.TRANSPARENT
            } else if (alpha < 0xFF) {
                transparency = TextureTransparencies.TRANSLUCENT
                break
            }
        }
        data.flip()

        this.data = data
        this.mipmapData = generateMipMaps(data)

        properties.postInit(this)


        state = TextureStates.LOADED
    }


    override fun toString(): String {
        return resourceLocation.toString()
    }
}
