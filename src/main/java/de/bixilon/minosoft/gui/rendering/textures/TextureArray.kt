/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.textures

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.assets.AssetsManager
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.util.logging.Log
import de.matthiasmann.twl.utils.PNGDecoder
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL12.glTexImage3D
import org.lwjgl.opengl.GL12.glTexSubImage3D
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER
import java.nio.ByteBuffer

class TextureArray(val allTextures: MutableList<Texture>) {
    val animator = Animator()
    private var textureIds = Array(TEXTURE_RESOLUTION_ID_MAP.size) { -1 }

    private val texturesByResolution = Array<MutableList<Texture>>(TEXTURE_RESOLUTION_ID_MAP.size) { mutableListOf() }

    fun preLoad(assetsManager: AssetsManager?) {
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

                texture.arraySinglePixelFactor = 1.0f / arrayResolution // ToDo: Why +1 ?? Still not working right
                texture.arrayLayer = it.size


                it.add(texture)
                texture.properties.animation?.let { properties ->
                    properties.animationId = animator.animatedTextures.size
                    animator.animatedTextures.add(TextureAnimation(texture))

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
        var totalLayers = 0
        for ((index, textures) in texturesByResolution.withIndex()) {
            loadResolution(index)
            totalLayers += textures.size
        }
        Log.game("Loaded ${allTextures.size} textures containing ${animator.animatedTextures.size} animated ones, split into $totalLayers layers!")

        animator.initBuffer()
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

        private const val INTS_PER_ANIMATED_TEXTURE = 4
    }

    inner class Animator {
        val animatedTextures: MutableList<TextureAnimation> = mutableListOf()
        private var animatedBufferDataId = -1

        var lastRun = 0L
        private lateinit var animatedData: IntArray

        var initialized = false
            private set

        fun initBuffer() {
            animatedData = IntArray(animatedTextures.size * INTS_PER_ANIMATED_TEXTURE) // 4 data ints per entry


            animatedBufferDataId = glGenBuffers()
            glBindBuffer(GL_UNIFORM_BUFFER, animatedBufferDataId)
            glBufferData(GL_UNIFORM_BUFFER, animatedData, GL_DYNAMIC_DRAW)
            glBindBuffer(GL_UNIFORM_BUFFER, 0)
            glBindBufferRange(GL_UNIFORM_BUFFER, 0, animatedBufferDataId, 0, animatedData.size.toLong())


            initialized = true
        }

        fun draw() {
            if (!initialized) {
                return
            }
            if (!Minosoft.getConfig().config.game.graphics.animations.textures) {
                return
            }

            val currentTime = System.currentTimeMillis()
            val deltaLastDraw = currentTime - lastRun
            lastRun = currentTime

            for (textureAnimation in animatedTextures) {
                var currentFrame = textureAnimation.getCurrentFrame()
                textureAnimation.currentTime += deltaLastDraw

                if (textureAnimation.currentTime >= currentFrame.animationTime) {
                    currentFrame = textureAnimation.getAndSetNextFrame()
                    textureAnimation.currentTime = 0L
                }

                val nextFrame = textureAnimation.getNextFrame()

                val interpolation = if (textureAnimation.animationProperties.interpolate) {
                    (textureAnimation.currentTime * 100) / currentFrame.animationTime
                } else {
                    0L
                }


                val baseAnimatedData = (textureAnimation.texture.arrayId shl 24) or textureAnimation.texture.arrayLayer

                val arrayOffset = textureAnimation.animationProperties.animationId * INTS_PER_ANIMATED_TEXTURE

                animatedData[arrayOffset] = baseAnimatedData + currentFrame.index
                animatedData[arrayOffset + 1] = baseAnimatedData + nextFrame.index
                animatedData[arrayOffset + 2] = interpolation.toInt()
            }


            uploadAnimatedStorageBuffer()
        }


        fun use(shader: Shader, bufferName: String) {
            shader.use()

            shader.setUniformBuffer(bufferName, 0)
        }

        private fun uploadAnimatedStorageBuffer() {
            glBindBuffer(GL_UNIFORM_BUFFER, animatedBufferDataId)
            glBufferSubData(GL_UNIFORM_BUFFER, 0, animatedData)
            glBindBuffer(GL_UNIFORM_BUFFER, 0)
        }
    }
}

private fun ByteBuffer.copyFrom(origin: ByteBuffer, sourceOffset: Int, destinationOffset: Int, length: Int) {
    origin.rewind()
    origin.position(sourceOffset)
    val bytes = ByteArray(length)

    origin.get(bytes, 0, length)

    this.put(bytes, destinationOffset, length)

}
