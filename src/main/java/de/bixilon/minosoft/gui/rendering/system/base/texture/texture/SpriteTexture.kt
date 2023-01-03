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
import de.bixilon.minosoft.gui.rendering.textures.properties.ImageProperties
import de.matthiasmann.twl.utils.PNGDecoder
import java.nio.ByteBuffer

class SpriteTexture(private val original: AbstractTexture) : AbstractTexture {
    override val resourceLocation: ResourceLocation = original.resourceLocation
    override var textureArrayUV: Vec2 by original::textureArrayUV
    override var atlasSize: Int by original::atlasSize
    override var singlePixelSize: Vec2 by original::singlePixelSize
    override var properties: ImageProperties by original::properties
    override var renderData: TextureRenderData by original::renderData
    override val transparency: TextureTransparencies by original::transparency
    override var generateMipMaps: Boolean = true

    override var state: TextureStates = TextureStates.DECLARED
        private set

    override var data: ByteBuffer? = null
    override var mipmapData: Array<ByteBuffer>? = null
    override var size: Vec2i = Vec2i(-1, -1)
        private set
    var splitTextures: MutableList<MemoryTexture> = mutableListOf()


    @Synchronized
    override fun load(assetsManager: AssetsManager) {
        if (state == TextureStates.LOADED) {
            return
        }
        original.load(assetsManager)

        val animationProperties = properties.animation!!
        size = Vec2i(animationProperties.width, animationProperties.height)


        val bytesPerTexture = size.x * size.y * PNGDecoder.Format.RGBA.numComponents

        for (i in 0 until animationProperties.frameCount) {
            val splitTexture = MemoryTexture(resourceLocation = ResourceLocation.of(resourceLocation.full + "_animated_$i"), size)

            splitTexture.data!!.let {
                it.copyFrom(original.data!!, bytesPerTexture * i, 0, bytesPerTexture)
                it.flip()
                splitTexture.mipmapData = splitTexture.generateMipMaps(it)
            }
            splitTextures += splitTexture
        }
        state = TextureStates.LOADED
    }

    companion object {
        private fun ByteBuffer.copyFrom(origin: ByteBuffer, sourceOffset: Int, destinationOffset: Int, length: Int) {
            origin.rewind()
            origin.position(sourceOffset)
            val bytes = ByteArray(length)

            origin.get(bytes, 0, length)

            this.put(bytes, destinationOffset, length)
        }
    }
}
