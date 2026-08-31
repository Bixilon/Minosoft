/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
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
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.gui.rendering.shader.types.TextureShader
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.TextureArrayStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.font.FontCompressions
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.font.FontTextureArray
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.RGB8Buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.RGBA8Buffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.system.base.texture.loader.file.PNGTextureLoader
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGlTextureUtil.glFormat
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGlTextureUtil.glType
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL33.GL_TEXTURE_SWIZZLE_RGBA
import java.nio.ByteBuffer

class OpenGlFontTextureArray(
    val system: OpenGlRenderSystem,
    compressed: FontCompressions,
) : FontTextureArray(system.context, RESOLUTION, compressed) {
    val index = system.nextTextureIndex++
    private var handle = -1


    override fun upload(latch: AbstractLatch?) {
        this.handle = OpenGlTextureUtil.createTextureArray(index, 0)

        // Texture alpha format is also available in OpenGL compatibility profile and WebGL but was removed in OpenGL core profile. An alternative is to rely on texture red format and texture swizzle as shown with the following code samples. (see https://www.g-truc.net/post-0734.html)
        val format = when (compression) {
            FontCompressions.NONE -> GL_RGBA8
            FontCompressions.ALPHA -> GL_R8
            FontCompressions.COMPRESSED_ALPHA -> GL_COMPRESSED_RED
        }
        if (compression != FontCompressions.NONE) {
            gl { glTexParameteriv(GL_TEXTURE_2D_ARRAY, GL_TEXTURE_SWIZZLE_RGBA, intArrayOf(GL_ONE, GL_ONE, GL_ONE, GL_RED)) }
        }

        gl { glTexImage3D(GL_TEXTURE_2D_ARRAY, 0, format, RESOLUTION, RESOLUTION, textures.size, 0, GL_RGBA, GL_UNSIGNED_BYTE, null as ByteBuffer?) }

        var index = 0
        for (texture in textures) {
            val size = texture.size

            val uvEnd = if (size.x == resolution && size.y == resolution) null else Vec2f(size) / resolution

            texture.renderData = OpenGlTextureData(this.index, index++, uvEnd)

            val buffer = texture.data.buffer
            val next = when {
                compression == FontCompressions.NONE -> RGBA8Buffer(texture.size)
                else -> buffer
            }

            next.data.position(0)
            next.data.limit(next.data.capacity())

            if (next is RGBA8Buffer) {
                buffer.copyAlphaToRGB(next)
            }

            if (compression != FontCompressions.NONE && texture.loader is PNGTextureLoader && next is RGBA8Buffer) {
                next.copyAlphaToRGB()
            }

            gl { glTexSubImage3D(GL_TEXTURE_2D_ARRAY, 0, 0, 0, index - 1, next.size.x, next.size.y, 1, next.glFormat, next.glType, next.data) }
        }

        Log.log(LogMessageType.RENDERING, LogLevels.VERBOSE) { "Loaded ${textures.size} font textures" }
        state = TextureArrayStates.UPLOADED
    }

    override fun use(shader: TextureShader, name: String) {
        if (state != TextureArrayStates.UPLOADED) throw IllegalStateException("Texture array is not uploaded yet! Are you trying to load a shader in the init phase?")
        shader.use()

        shader.native.setTexture("$name[$index]", index)
    }


    private fun load(texture: Texture) {
        if (texture.state != TextureStates.LOADED) texture.load(context)
    }

    override fun load(latch: AbstractLatch?) {
        if (state != TextureArrayStates.PREPARING) throw IllegalStateException("Already loaded!")
        system.log { "Loading font texture" }
        for (texture in textures) {
            load(texture)
        }
        state = TextureArrayStates.LOADED
    }

    private companion object {
        const val RESOLUTION = 1024

        private fun RGBA8Buffer.copyAlphaToRGB() {
            val pixels = data.limit() / bytes
            for (index in 0 until pixels) {
                val offset = index * bytes
                val alpha = data[offset + 3]
                data.put(offset + 0, alpha)
                // this.put(offset + 1, alpha)
                // this.put(offset + 2, alpha)
            }
        }

        private fun TextureBuffer.copyAlphaToRGB(next: RGBA8Buffer) {
            assert(size == next.size)

            for (x in 0 until size.x) {
                for (y in 0 until size.y) {
                    val alpha = this.getA(x, y)
                    next.setRGBA(x, y, 0xFF, 0xFF, 0xFF, alpha)
                }
            }
        }
    }
}
