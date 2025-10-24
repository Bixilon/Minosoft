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

package de.bixilon.minosoft.gui.rendering.system.opengl.texture

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.shader.types.TextureShader
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.StaticTextureArray
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.TextureArrayProperties
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.TextureArrayStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.TextureData
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGLTextureUtil.glFormat
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGLTextureUtil.glType
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.lwjgl.opengl.GL13.*
import org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY
import java.nio.ByteBuffer

class OpenGLTextureArray(
    context: RenderContext,
    async: Boolean,
    mipmaps: Int,
) : StaticTextureArray(context, async, mipmaps) {
    private var handles = IntArray(RESOLUTIONS.size) { -1 }

    private val resolution = Array<MutableList<Texture>>(RESOLUTIONS.size) { mutableListOf() }
    private val lastTextureId = IntArray(RESOLUTIONS.size)

    init {
        context.system.unsafeCast<OpenGlRenderSystem>().textureBindingIndex += RESOLUTIONS.size
    }

    override fun activate() {
        context.system.unsafeCast<OpenGlRenderSystem>().log { "Activating static texture array" }
        for ((index, textureId) in handles.withIndex()) {
            if (textureId == -1) {
                continue
            }
            gl { glActiveTexture(GL_TEXTURE0 + index) }
            gl { glBindTexture(GL_TEXTURE_2D_ARRAY, textureId) }
        }
    }

    override fun use(shader: TextureShader, name: String) {
        if (state != TextureArrayStates.UPLOADED) throw IllegalStateException("Texture array is not uploaded yet! Are you trying to load a shader in the init phase?")
        context.system.unsafeCast<OpenGlRenderSystem>().log { "Binding static textures to $shader" }
        shader.use()
        activate()

        for ((index, textureId) in handles.withIndex()) {
            if (textureId == -1) {
                continue
            }

            gl { glBindTexture(GL_TEXTURE_2D_ARRAY, textureId) }
            shader.native.setTexture("$name[$index]", index)
        }
    }

    override fun findResolution(size: Vec2i): Vec2i {
        if (size.x >= MAX_RESOLUTION || size.y >= MAX_RESOLUTION) return Vec2i(MAX_RESOLUTION)
        val array = findArray(size)
        if (array < 0) return Vec2i(0, 0)
        return Vec2i(RESOLUTIONS[array])
    }

    private fun findArray(size: Vec2i): Int {
        for ((index, resolution) in RESOLUTIONS.withIndex()) {
            if (size.x > resolution || size.y > resolution) continue
            return index
        }

        return -1
    }

    private fun prepareUpload(arrayId: Int, texture: Texture) {
        val resolution = RESOLUTIONS[arrayId]
        val pixel = PIXEL[arrayId]
        val size = texture.size

        val uvEnd = if (size.x == resolution && size.y == resolution) null else Vec2f(size) / resolution
        val array = TextureArrayProperties(uvEnd, resolution, pixel)

        val animation = texture.animation
        if (animation == null) {
            this.resolution[arrayId] += texture
            texture.renderData = OpenGLTextureData(arrayId, lastTextureId[arrayId]++, uvEnd, -1)
            texture.array = array
            return
        }

        texture.renderData = OpenGLTextureData(-1, -1, uvEnd, animation.animationData)

        for (sprite in animation.sprites) {
            sprite.renderData = OpenGLTextureData(arrayId, lastTextureId[arrayId]++, uvEnd, animation.animationData)
            sprite.array = array
            this.resolution[arrayId] += sprite
        }
    }


    private fun upload(resolution: Int, textures: List<Texture>): Int {
        context.system.unsafeCast<OpenGlRenderSystem>().log { "Uploading ${resolution}x${resolution} static textures" }
        val handle = OpenGLTextureUtil.createTextureArray(mipmaps)

        for (level in 0..mipmaps) {
            gl { glTexImage3D(GL_TEXTURE_2D_ARRAY, level, GL_RGBA8, resolution shr level, resolution shr level, textures.size, 0, GL_RGBA, GL_UNSIGNED_BYTE, null as ByteBuffer?) }
        }

        for (texture in textures) {
            val renderData = texture.renderData as OpenGLTextureData
            for ((level, buffer) in texture.data.collect().withIndex()) {
                if (level > this.mipmaps) break
                buffer.data.position(0)
                buffer.data.limit(buffer.data.capacity())
                gl { glTexSubImage3D(GL_TEXTURE_2D_ARRAY, level, 0, 0, renderData.index, buffer.size.x, buffer.size.y, 1, buffer.glFormat, buffer.glType, buffer.data) }
            }

            texture.data = TextureData.NULL
        }

        return handle
    }


    override fun upload(textures: Collection<Texture>) {
        if (state != TextureArrayStates.LOADED) throw IllegalStateException("Not loaded!")

        for (texture in textures) {
            if (texture.size.x > MAX_RESOLUTION || texture.size.y > MAX_RESOLUTION) {
                Log.log(LogMessageType.LOADING, LogLevels.WARN) { "Texture $texture exceeds max resolution ($MAX_RESOLUTION): ${texture.size}" }
                continue
            }

            val arrayId = findArray(texture.size)
            if (arrayId == -1) {
                Log.log(LogMessageType.LOADING, LogLevels.WARN) { "Can not find texture array for $arrayId" }
                continue
            }
            prepareUpload(arrayId, texture)
        }
    }

    override fun upload(latch: AbstractLatch?) {
        super.upload(latch)
        var total = 0
        for ((index, textures) in resolution.withIndex()) {
            if (textures.isEmpty()) continue
            handles[index] = upload(RESOLUTIONS[index], textures)
            total += textures.size
        }
        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Loaded ${named.size} textures containing ${animator.size} animated ones, split into $total layers!" }

        animator.init()
        state = TextureArrayStates.UPLOADED
    }

    private companion object {
        const val MAX_RESOLUTION = 2048
        val RESOLUTIONS = intArrayOf(16, 32, 64, 128, 256, 512, 1024, MAX_RESOLUTION) // A 12x12 texture will be saved in texture id 0 (in 0 are only 16x16 textures). Animated textures get split
        val PIXEL = FloatArray(RESOLUTIONS.size) { 1.0f / RESOLUTIONS[it] }
    }
}
