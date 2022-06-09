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

package de.bixilon.minosoft.gui.rendering.system.opengl.texture.dynamic

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.shader.Shader
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTexture
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureArray
import de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic.DynamicTextureState
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
    val renderWindow: RenderWindow,
    val index: Int = 7,
    val initialSize: Int = 32,
    val resolution: Int,
) : DynamicTextureArray {
    private var textures: Array<WeakReference<OpenGLDynamicTexture>?> = arrayOfNulls(initialSize)
    private var textureId = -1
    var shaders: MutableSet<Shader> = mutableSetOf()

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

    override fun pushArray(identifier: UUID, force: Boolean, data: () -> ByteArray): DynamicTexture {
        return pushBuffer(identifier, force) { ByteBuffer.wrap(data()) }
    }

    private fun load(texture: OpenGLDynamicTexture, index: Int, mipmaps: Array<ByteBuffer>) {
        glBindTexture(GL_TEXTURE_2D_ARRAY, textureId)

        for ((level, mipmap) in mipmaps.withIndex()) {
            glTexSubImage3D(GL_TEXTURE_2D_ARRAY, level, 0, 0, index, resolution shr level, resolution shr level, 1, GL_RGBA, GL_UNSIGNED_BYTE, mipmap)
        }

        texture.state = DynamicTextureState.LOADED
        renderWindow.textureManager.staticTextures.activate()
    }

    @Synchronized
    override fun pushBuffer(identifier: UUID, force: Boolean, data: () -> ByteBuffer): OpenGLDynamicTexture {
        check(textureId >= 0) { "Dynamic texture array not yet initialized!" }
        cleanup()
        for (textureReference in textures) {
            val texture = textureReference?.get()
            if (texture?.uuid == identifier) {
                return texture
            }
        }
        val index = getNextIndex()
        val texture = OpenGLDynamicTexture(identifier, createShaderIdentifier(index = index))
        textures[index] = WeakReference(texture)
        texture.state = DynamicTextureState.LOADING

        fun load() {
            val bytes = data()

            if (bytes.limit() > resolution * resolution * 4 || bytes.limit() < resolution * 4) { // allow anything in 1..resolution for y size
                Log.log(LogMessageType.ASSETS, LogLevels.WARN) { "Dynamic texture $texture, has not a size of ${resolution}x${resolution}!" }
                textures[index] = null
                texture.state = DynamicTextureState.UNLOADED
                return
            }

            val mipmaps = OpenGLTextureUtil.generateMipMaps(bytes, Vec2i(resolution, bytes.limit() / 4 / resolution))
            texture.data = mipmaps
            if (force) {
                load(texture, index, mipmaps)
            } else {
                renderWindow.queue += { load(texture, index, mipmaps) }
            }
        }

        if (force) {
            check(Thread.currentThread() == renderWindow.thread) { "Thread mismatch: ${Thread.currentThread()}" }
            load()
        } else {
            DefaultThreadPool += { load() }
        }
        return texture
    }

    private fun createShaderIdentifier(array: Int = this.index, index: Int): Int {
        check(array >= 0 && index >= 0) { "Array not initialized or index < 0" }
        return (array shl 28) or (index shl 12) or 0
    }


    override fun load(latch: CountUpAndDownLatch) {
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

    override fun use(shader: Shader, name: String) {
        shaders += shader
        _use(shader, name)
    }

    private fun _use(shader: Shader, name: String = "uTextures") {
        shader.use()

        activate()

        shader.setTexture("$name[$index]", index)
    }

    private fun getNextIndex(): Int {
        for ((index, texture) in textures.withIndex()) {
            if (texture == null) {
                return index
            }
        }
        val nextIndex = textures.size
        grow()
        return nextIndex
    }

    private fun reload() {
        glDeleteTextures(textureId)
        load(CountUpAndDownLatch(0))

        for ((index, textureReference) in textures.withIndex()) {
            val texture = textureReference?.get() ?: continue
            load(texture, index, texture.data ?: continue)
        }

        for (shader in shaders) {
            _use(shader)
        }
    }

    private fun grow() {
        val textures: Array<WeakReference<OpenGLDynamicTexture>?> = arrayOfNulls(textures.size + initialSize)
        for ((index, texture) in this.textures.withIndex()) {
            textures[index] = texture
        }
        this.textures = textures
        renderWindow.queue += { reload() }
    }

    @Synchronized
    private fun cleanup() {
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
    }
}
