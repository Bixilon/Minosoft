/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.textures

import de.bixilon.minosoft.data.assets.MinecraftAssetsManager
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.matthiasmann.twl.utils.PNGDecoder
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL12.glTexImage3D
import org.lwjgl.opengl.GL12.glTexSubImage3D
import org.lwjgl.opengl.GL13.*
import org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY
import java.nio.ByteBuffer

class TextureArray(val allTextures: MutableList<Texture>) {
    private var textureIds = Array(TEXTURE_RESOLUTION_ID_MAP.size) { -1 }

    private val texturesByResolution = Array<MutableList<Texture>>(TEXTURE_RESOLUTION_ID_MAP.size) { mutableListOf() }


    fun preLoad(assetsManager: MinecraftAssetsManager?) {
        for (texture in allTextures) {
            if (!texture.isLoaded) {
                texture.load(assetsManager!!)
            }
            check(texture.size.x <= TEXTURE_MAX_RESOLUTION) { "Texture's width exceeds $TEXTURE_MAX_RESOLUTION (${texture.size.x}" }
            check(texture.size.y <= TEXTURE_MAX_RESOLUTION) { "Texture's height exceeds $TEXTURE_MAX_RESOLUTION (${texture.size.y}" }

            texture.properties.postInit(texture)

            texture.properties.animation?.let {
                texture.size = Vec2i(it.width, it.height)
            }
            val size = texture.size

            for (i in TEXTURE_RESOLUTION_ID_MAP.indices) {
                val currentResolution = TEXTURE_RESOLUTION_ID_MAP[i]
                if (size.x <= currentResolution && size.y <= currentResolution) {
                    texture.arrayId = i
                    break
                }
            }

            texturesByResolution[texture.arrayId].let {
                val arrayResolution = TEXTURE_RESOLUTION_ID_MAP[texture.arrayId]


                texture.uvEnd = Vec2(
                    x = size.x.toFloat() / arrayResolution,
                    y = size.y.toFloat() / arrayResolution
                )

                texture.arraySinglePixelSize = 1.0f / arrayResolution
                texture.arrayLayer = it.size


                it.add(texture)
                texture.properties.animation?.let { properties ->
                    val bytesPerTexture = size.x * size.y * PNGDecoder.Format.RGBA.numComponents
                    val fullBuffer = texture.buffer!!
                    texture.buffer = BufferUtils.createByteBuffer(bytesPerTexture)
                    texture.buffer!!.copyFrom(fullBuffer, 0, 0, bytesPerTexture)
                    texture.buffer!!.flip()

                    for (i in 0 until properties.frameCount) {
                        val splitTexture = Texture(resourceLocation = ResourceLocation(texture.resourceLocation.full + "_animated_$i"))

                        splitTexture.inherit(texture)
                        splitTexture.arrayLayer = it.size
                        splitTexture.buffer = BufferUtils.createByteBuffer(bytesPerTexture)
                        splitTexture.buffer!!.copyFrom(fullBuffer, bytesPerTexture * i, 0, bytesPerTexture)
                        splitTexture.buffer!!.flip()

                        it.add(splitTexture)
                    }
                }
            }
        }
    }

    fun load() {
        for (index in texturesByResolution.indices) {
            loadResolution(index)
        }
    }

    private fun loadResolution(resolutionId: Int) {
        val resolution = TEXTURE_RESOLUTION_ID_MAP[resolutionId]
        val textures = texturesByResolution[resolutionId]

        val textureId = glGenTextures()
        textureIds[resolutionId] = textureId
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureId)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_S, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_WRAP_T, GL_REPEAT)
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
        // glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR) // ToDo: This breaks transparency again
        glTexParameteri(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

        glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, GL_RGBA, resolution, resolution, textures.size, 0, GL_RGBA, GL_UNSIGNED_BYTE, null as ByteBuffer?)

        for (texture in textures) {
            glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, texture.arrayLayer, texture.size.x, texture.size.y, 1, GL_RGBA, GL_UNSIGNED_BYTE, texture.buffer!!)
            texture.buffer = null
        }
        //  glGenerateMipmap(GL_TEXTURE_2D_ARRAY)
    }


    fun use(shader: Shader, arrayName: String) {
        shader.use()

        for ((index, textureId) in textureIds.withIndex()) {
            glActiveTexture(GL_TEXTURE0 + index)
            glBindTexture(GL_TEXTURE_2D_ARRAY, textureId)
            shader.setTexture("$arrayName[$index]", index)
        }
    }

    companion object {
        val TEXTURE_RESOLUTION_ID_MAP = arrayOf(16, 32, 64, 128, 256, 512, 1024) // A 12x12 texture will be saved in texture id 0 (in 0 are only 16x16 textures). Animated textures get split
        const val TEXTURE_MAX_RESOLUTION = 1024

        val DEBUG_TEXTURE = Texture.getResourceTextureIdentifier(textureName = "block/debug")
    }
}

private fun ByteBuffer.copyFrom(origin: ByteBuffer, sourceOffset: Int, destinationOffset: Int, length: Int) {
    origin.rewind()
    origin.position(sourceOffset)
    val bytes = ByteArray(length)

    origin.get(bytes, 0, length)

    this.put(bytes, destinationOffset, length)

}
