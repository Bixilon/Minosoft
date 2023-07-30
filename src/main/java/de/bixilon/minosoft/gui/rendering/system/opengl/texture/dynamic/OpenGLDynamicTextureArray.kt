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

package de.bixilon.minosoft.gui.rendering.system.opengl.texture.dynamic

import de.bixilon.kutil.concurrent.lock.thread.ThreadLock
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.shader.ShaderUniforms
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.TextureData
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureArray
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureState
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.texture.OpenGLTextureUtil
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.glTexImage3D
import org.lwjgl.opengl.GL12.glTexSubImage3D
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL30.GL_TEXTURE_2D_ARRAY
import java.lang.ref.WeakReference
import java.nio.ByteBuffer
import java.util.*

class OpenGLDynamicTextureArray(
    val context: RenderContext,
    val renderSystem: OpenGLRenderSystem,
    val index: Int = 7,
    val initialSize: Int = 32,
    val resolution: Int,
) : DynamicTextureArray {
    private var textures: Array<WeakReference<OpenGLDynamicTexture>?> = arrayOfNulls(initialSize)
    private val lock = ThreadLock()
    private var textureId = -1
    var shaders: MutableSet<NativeShader> = mutableSetOf()

    override val size: Int
        get() {
            var size = 0
            for (texture in textures) {
                if (texture == null) {
                    continue
                }
                size++
            }
            return size
        }

    private fun load(texture: OpenGLDynamicTexture, index: Int, mipmaps: Array<ByteBuffer>) {
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureId)

        for ((level, mipmap) in mipmaps.withIndex()) {
            // glTexSubImage3D(GL_TEXTURE_2D_ARRAY, level, 0, 0, index, texture.size.x shr level, texture.size.y shr level, 1, GL_RGBA, GL_UNSIGNED_BYTE, mipmap)
            glTexSubImage3D(GL_TEXTURE_2D_ARRAY, level, 0, 0, index, resolution shr level, resolution shr level, 1, GL_RGBA, GL_UNSIGNED_BYTE, mipmap)
        }

        texture.state = DynamicTextureState.LOADED
        context.textures.staticTextures.activate()
    }

    override fun pushBuffer(identifier: UUID, force: Boolean, data: () -> TextureData): OpenGLDynamicTexture {
        lock.lock()
        check(textureId >= 0) { "Dynamic texture array not yet initialized!" }
        cleanup()
        for (textureReference in textures) {
            val texture = textureReference?.get()
            if (texture?.uuid == identifier) {
                lock.unlock()
                return texture
            }
        }
        val index = getNextIndex()
        val texture = OpenGLDynamicTexture(identifier, createShaderIdentifier(index = index))
        textures[index] = WeakReference(texture)
        texture.state = DynamicTextureState.LOADING

        fun load() {
            val data = data()

            if (data.buffer.limit() > resolution * resolution * 4 || data.buffer.limit() < resolution * 4) { // allow anything in 1..resolution for y size
                Log.log(LogMessageType.ASSETS, LogLevels.WARN) { "Dynamic texture $texture, has not a size of ${resolution}x${resolution}!" }
            }

            val mipmaps = OpenGLTextureUtil.generateMipMaps(data.buffer, data.size)
            texture.data = mipmaps
            texture.size = data.size
            if (force) {
                load(texture, index, mipmaps) // thread check already done
            } else {
                context.queue += { load(texture, index, mipmaps) }
            }
        }

        if (force) {
            renderSystem.assertThread()
            load()
        } else {
            DefaultThreadPool += { load() }
        }
        lock.unlock()
        return texture
    }

    private fun createShaderIdentifier(array: Int = this.index, index: Int): Int {
        check(array >= 0 && index >= 0) { "Array not initialized or index < 0" }
        return (array shl 28) or (index shl 12) or 0
    }


    override fun load(latch: AbstractLatch?) {
        val textureId = OpenGLTextureUtil.createTextureArray()
        this.textureId = textureId


        for (level in 0 until OpenGLTextureUtil.MAX_MIPMAP_LEVELS) {
            glTexImage3D(GL_TEXTURE_2D_ARRAY, level, GL_RGBA, resolution shr level, resolution shr level, textures.size, 0, GL_RGBA, GL_UNSIGNED_BYTE, null as ByteBuffer?)
        }

        this.textureId = textureId
    }

    override fun activate() {
        glActiveTexture(GL_TEXTURE0 + index)
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureId)
    }

    override fun use(shader: NativeShader, name: String) {
        shaders += shader
        _use(shader, name)
    }

    private fun _use(shader: NativeShader, name: String = ShaderUniforms.TEXTURES) {
        shader.use()

        activate()

        shader.setTexture("$name[$index]", index)
    }

    private fun getNextIndex(): Int {
        lock.lock()
        for ((index, texture) in textures.withIndex()) {
            if (texture == null) {
                lock.unlock()
                return index
            }
        }
        val nextIndex = textures.size
        grow()
        lock.unlock()
        return nextIndex
    }

    private fun reload() {
        lock.lock()
        glDeleteTextures(textureId)
        load(null)

        for ((index, textureReference) in textures.withIndex()) {
            val texture = textureReference?.get() ?: continue
            load(texture, index, texture.data ?: continue)
        }

        for (shader in shaders) {
            _use(shader)
        }
        lock.unlock()
    }

    private fun grow() {
        lock.lock()
        val textures: Array<WeakReference<OpenGLDynamicTexture>?> = arrayOfNulls(textures.size + initialSize)
        for ((index, texture) in this.textures.withIndex()) {
            textures[index] = texture
        }
        this.textures = textures
        context.queue += { reload() }
        lock.unlock()
    }

    @Synchronized
    private fun cleanup() {
        lock.lock()
        for ((index, reference) in textures.withIndex()) {
            if (reference == null) {
                continue
            }
            val texture = reference.get()
            if (texture == null) {
                textures[index] = null
                continue
            }
            if (texture.usages.get() > 0) {
                continue
            }
            texture.state = DynamicTextureState.UNLOADED
            textures[index] = null
        }
        lock.unlock()
    }
}
