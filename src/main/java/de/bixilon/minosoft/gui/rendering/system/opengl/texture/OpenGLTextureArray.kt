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
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.pool.runnable.SimplePoolRunnable
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.latch.SimpleLatch
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.StaticTextureArray
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.TextureArrayProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.TextureArrayStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.TextureData
import de.bixilon.minosoft.gui.rendering.system.base.texture.sprite.SpriteTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGLTextureUtil.glFormat
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGLTextureUtil.glType
import de.bixilon.minosoft.gui.rendering.textures.TextureAnimation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.lwjgl.opengl.GL13.*
import org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicInteger

class OpenGLTextureArray(
    context: RenderContext,
    async: Boolean,
    mipmaps: Int,
) : StaticTextureArray(context, async, mipmaps) {
    private var textureIds = IntArray(TEXTURE_RESOLUTION_ID_MAP.size) { -1 }

    private val texturesByResolution = Array<MutableList<Texture>>(TEXTURE_RESOLUTION_ID_MAP.size) { mutableListOf() }
    private val lastTextureId = IntArray(TEXTURE_RESOLUTION_ID_MAP.size)

    init {
        context.system.unsafeCast<OpenGLRenderSystem>().textureBindingIndex += TEXTURE_RESOLUTION_ID_MAP.size
    }


    private fun preLoad(latch: AbstractLatch, textures: Collection<Texture>) {
        for (texture in textures) {
            if (texture.state != TextureStates.DECLARED) {
                latch.dec()
                continue
            }
            DefaultThreadPool += SimplePoolRunnable(ThreadPool.HIGH) { texture.load(context); latch.dec() }
        }
    }

    private fun preLoad(animationIndex: AtomicInteger, textures: Collection<Texture>) {
        for (texture in textures) {
            check(texture.size.x <= TEXTURE_MAX_RESOLUTION) { "Texture's width exceeds $TEXTURE_MAX_RESOLUTION (${texture.size.x})" }
            check(texture.size.y <= TEXTURE_MAX_RESOLUTION) { "Texture's height exceeds $TEXTURE_MAX_RESOLUTION (${texture.size.y})" }


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
            val array = TextureArrayProperties(uvEnd ?: Vec2(1.0f, 1.0f), arrayResolution, singlePixelSize)

            if (texture is SpriteTexture) {
                val animationIndex = animationIndex.getAndIncrement()
                val animation = TextureAnimation(texture)
                animator.animations += animation
                texture.renderData = OpenGLTextureData(-1, -1, uvEnd, animationIndex)
                for (split in texture.splitTextures) {
                    split.renderData = OpenGLTextureData(arrayId, lastTextureId[arrayId]++, uvEnd, animationIndex)
                    split.array = array
                    texturesByResolution[arrayId] += split
                }
                for (frame in texture.properties.animation!!.frames) {
                    frame.texture = texture.splitTextures[frame.index]
                }
            } else {
                texturesByResolution[arrayId] += texture
                texture.renderData = OpenGLTextureData(arrayId, lastTextureId[arrayId]++, uvEnd, -1)
                texture.array = array
            }
        }
    }

    @Synchronized
    override fun load(latch: AbstractLatch) {
        if (state == TextureArrayStates.LOADED || state == TextureArrayStates.PRE_LOADED) {
            return
        }
        val preLoadLatch = SimpleLatch(named.size + other.size)
        preLoad(preLoadLatch, named.values)
        preLoad(preLoadLatch, other)
        preLoadLatch.await()

        val animationIndex = AtomicInteger()
        preLoad(animationIndex, named.values)
        preLoad(animationIndex, other)

        state = TextureArrayStates.PRE_LOADED
    }


    private fun loadSingleArray(resolution: Int, textures: List<Texture>): Int {
        val textureId = OpenGLTextureUtil.createTextureArray(mipmaps)

        for (level in 0..mipmaps) {
            glTexImage3D(GL_TEXTURE_2D_ARRAY, level, GL_RGBA8, resolution shr level, resolution shr level, textures.size, 0, GL_RGBA, GL_UNSIGNED_BYTE, null as ByteBuffer?)
        }

        for (texture in textures) {
            val renderData = texture.renderData as OpenGLTextureData
            for ((level, buffer) in texture.data.collect().withIndex()) {
                if (level > this.mipmaps) break
                buffer.data.flip()
                glTexSubImage3D(GL_TEXTURE_2D_ARRAY, level, 0, 0, renderData.index, buffer.size.x, buffer.size.y, 1, buffer.glFormat, buffer.glType, buffer.data)
            }

            texture.data = TextureData.NULL
        }

        return textureId
    }


    override fun upload(latch: AbstractLatch?) {
        var totalLayers = 0
        for ((index, textures) in texturesByResolution.withIndex()) {
            if (textures.isEmpty()) {
                continue
            }
            textureIds[index] = loadSingleArray(TEXTURE_RESOLUTION_ID_MAP[index], textures)
            totalLayers += textures.size
        }
        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Loaded ${named.size} textures containing ${animator.animations.size} animated ones, split into $totalLayers layers!" }

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
        const val TEXTURE_MAX_RESOLUTION = 2048
        val TEXTURE_RESOLUTION_ID_MAP = intArrayOf(16, 32, 64, 128, 256, 512, 1024, TEXTURE_MAX_RESOLUTION) // A 12x12 texture will be saved in texture id 0 (in 0 are only 16x16 textures). Animated textures get split
    }
}
