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

package de.bixilon.minosoft.gui.rendering.system.opengl.texture

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.assets.util.FileUtil.readAsString
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.texture.SpriteAnimator
import de.bixilon.minosoft.gui.rendering.system.base.texture.StaticTextureArray
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureArrayStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.SpriteTexture
import de.bixilon.minosoft.gui.rendering.textures.TextureAnimation
import de.bixilon.minosoft.gui.rendering.textures.properties.ImageProperties
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.lwjgl.opengl.GL12.*
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY
import java.nio.ByteBuffer

class OpenGLTextureArray(
    val context: RenderContext,
    private val loadTexturesAsync: Boolean = true,
    override val textures: MutableMap<ResourceLocation, AbstractTexture> = mutableMapOf(),
) : StaticTextureArray {
    override val animator = SpriteAnimator(context.renderSystem)
    private var textureIds = IntArray(TEXTURE_RESOLUTION_ID_MAP.size) { -1 }
    override var state: TextureArrayStates = TextureArrayStates.DECLARED
        private set

    private val texturesByResolution = Array<MutableList<AbstractTexture>>(TEXTURE_RESOLUTION_ID_MAP.size) { mutableListOf() }
    private val lastTextureId = IntArray(TEXTURE_RESOLUTION_ID_MAP.size)


    @Synchronized
    override fun createTexture(resourceLocation: ResourceLocation, mipmaps: Boolean, default: () -> AbstractTexture): AbstractTexture {
        textures[resourceLocation]?.let { return it }

        // load .mcmeta
        val properties = readImageProperties(resourceLocation) ?: ImageProperties()

        val texture = if (properties.animation == null) {
            default()
        } else {
            SpriteTexture(default().apply { generateMipMaps = false })
        }

        texture.properties = properties
        textures[resourceLocation] = texture
        if (loadTexturesAsync) {
            DefaultThreadPool += { texture.load(context.connection.assetsManager) }
        }

        return texture
    }

    private fun readImageProperties(texture: ResourceLocation): ImageProperties? {
        try {
            val data = context.connection.assetsManager.getOrNull("$texture.mcmeta".toResourceLocation())?.readAsString() ?: return null
            return Jackson.MAPPER.readValue(data, ImageProperties::class.java)
        } catch (error: Throwable) {
            error.printStackTrace()
        }
        return null
    }

    @Synchronized
    override fun preLoad(latch: CountUpAndDownLatch) {
        if (state == TextureArrayStates.LOADED || state == TextureArrayStates.PRE_LOADED) {
            return
        }
        var lastAnimationIndex = 0
        for (texture in textures.values) {
            if (texture.state == TextureStates.DECLARED) {
                texture.load(context.connection.assetsManager)
            }

            check(texture.size.x <= TEXTURE_MAX_RESOLUTION) { "Texture's width exceeds $TEXTURE_MAX_RESOLUTION (${texture.size.x}" }
            check(texture.size.y <= TEXTURE_MAX_RESOLUTION) { "Texture's height exceeds $TEXTURE_MAX_RESOLUTION (${texture.size.y}" }


            var arrayId = -1
            var arrayResolution = -1

            for (i in TEXTURE_RESOLUTION_ID_MAP.indices) {
                arrayResolution = TEXTURE_RESOLUTION_ID_MAP[i]
                if (texture.size.x <= arrayResolution && texture.size.y <= arrayResolution) {
                    arrayId = i
                    break
                }
            }


            val uvEnd: Vec2? = if (texture.size.x == arrayResolution && texture.size.y == arrayResolution) {
                null
            } else {
                Vec2(texture.size) / arrayResolution
            }
            val singlePixelSize = Vec2(1.0f) / arrayResolution

            if (texture is SpriteTexture) {
                val animationIndex = lastAnimationIndex++
                val animation = TextureAnimation(texture)
                animator.animations += animation
                texture.renderData = OpenGLTextureData(-1, -1, uvEnd, animationIndex)
                for (split in texture.splitTextures) {
                    split.renderData = OpenGLTextureData(arrayId, lastTextureId[arrayId]++, uvEnd, animationIndex)
                    split.singlePixelSize = singlePixelSize
                    texturesByResolution[arrayId] += split
                }
                for (frame in texture.properties.animation!!.frames) {
                    frame.texture = texture.splitTextures[frame.index]
                }
            } else {
                texturesByResolution[arrayId] += texture
                texture.renderData = OpenGLTextureData(arrayId, lastTextureId[arrayId]++, uvEnd, -1)
                texture.singlePixelSize = singlePixelSize
                texture.textureArrayUV = uvEnd ?: Vec2(1.0f, 1.0f)
                texture.atlasSize = arrayResolution
            }
        }

        state = TextureArrayStates.PRE_LOADED
    }


    @Synchronized
    private fun loadSingleArray(resolution: Int, textures: List<AbstractTexture>): Int {
        val textureId = OpenGLTextureUtil.createTextureArray()

        for (level in 0 until OpenGLTextureUtil.MAX_MIPMAP_LEVELS) {
            glTexImage3D(GL_TEXTURE_2D_ARRAY, level, GL_RGBA, resolution shr level, resolution shr level, textures.size, 0, GL_RGBA, GL_UNSIGNED_BYTE, null as ByteBuffer?)
        }

        for (texture in textures) {
            val mipMaps = texture.mipmapData ?: throw IllegalStateException("Texture not loaded properly!")

            val renderData = texture.renderData as OpenGLTextureData
            for ((level, data) in mipMaps.withIndex()) {
                val size = texture.size shr level

                glTexSubImage3D(GL_TEXTURE_2D_ARRAY, level, 0, 0, renderData.index, size.x, size.y, 1, GL_RGBA, GL_UNSIGNED_BYTE, data)
            }

            texture.data = null
            texture.mipmapData = null
        }

        return textureId
    }


    @Synchronized
    override fun load(latch: CountUpAndDownLatch) {
        var totalLayers = 0
        for ((index, textures) in texturesByResolution.withIndex()) {
            if (textures.isEmpty()) {
                continue
            }
            textureIds[index] = loadSingleArray(TEXTURE_RESOLUTION_ID_MAP[index], textures)
            totalLayers += textures.size
        }
        Log.log(LogMessageType.RENDERING_LOADING, LogLevels.VERBOSE) { "Loaded ${textures.size} textures containing ${animator.animations.size} animated ones, split into $totalLayers layers!" }

        animator.init()
        state = TextureArrayStates.LOADED
    }

    override fun activate() {
        for ((index, textureId) in textureIds.withIndex()) {
            if (textureId == -1) {
                continue
            }
            glActiveTexture(GL_TEXTURE0 + index)
            glBindTexture(GL_TEXTURE_2D_ARRAY, textureId)
        }
    }

    override fun use(shader: NativeShader, name: String) {
        shader.use()
        activate()

        for ((index, textureId) in textureIds.withIndex()) {
            if (textureId == -1) {
                continue
            }

            glBindTexture(GL_TEXTURE_2D_ARRAY, textureId)
            shader.setTexture("$name[$index]", index)
        }
    }


    companion object {
        const val TEXTURE_MAX_RESOLUTION = 1024
        val TEXTURE_RESOLUTION_ID_MAP = intArrayOf(16, 32, 64, 128, 256, 512, TEXTURE_MAX_RESOLUTION) // A 12x12 texture will be saved in texture id 0 (in 0 are only 16x16 textures). Animated textures get split
    }
}
