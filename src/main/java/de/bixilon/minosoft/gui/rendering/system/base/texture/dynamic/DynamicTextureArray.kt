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

package de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic

import de.bixilon.kutil.concurrent.lock.thread.ThreadLock
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.runnable.ForcePooledRunnable
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.gui.rendering.system.base.shader.ShaderUniforms
import de.bixilon.minosoft.gui.rendering.system.base.texture.array.TextureArray
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.MipmapTextureData
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import de.bixilon.minosoft.gui.rendering.textures.TextureUtil.readTexture
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.io.ByteArrayInputStream
import java.lang.ref.WeakReference

abstract class DynamicTextureArray(
    val context: RenderContext,
    val initialSize: Int,
    val mipmaps: Int,
) : TextureArray {
    protected var textures: Array<WeakReference<DynamicTexture>?> = arrayOfNulls(initialSize)
    protected val shaders: MutableSet<NativeShader> = mutableSetOf()
    private val lock = ThreadLock()
    private var reload = false

    val capacity get() = textures.size

    val size: Int
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


    operator fun get(identifier: Any): DynamicTexture? {
        lock.acquire()
        val texture = unsafeGet(identifier)
        lock.release()
        return texture
    }

    fun unsafeGet(identifier: Any): DynamicTexture? {
        for (reference in textures) {
            val texture = reference?.get() ?: continue
            if (texture.identifier == identifier) {
                return texture
            }
        }
        return null
    }

    private fun DynamicTexture.load(index: Int, creator: () -> TextureBuffer) {
        val buffer = try {
            creator.invoke()
        } catch (error: Throwable) {
            Log.log(LogMessageType.RENDERING, LogLevels.WARN) { "Could not load dynamic texture (index=$index, identifier=${this.identifier}): $error" }
            error.printStackTrace()
            this.state = DynamicTextureState.ERROR
            return
        }

        this.data = MipmapTextureData(buffer, mipmaps)
        if (Thread.currentThread() == context.thread) {
            upload(index, this)
        } else {
            context.queue += { upload(index, this) }
        }
    }

    fun pushRaw(identifier: Any, async: Boolean = true, creator: () -> ByteArray): DynamicTexture {
        return push(identifier, async) { ByteArrayInputStream(creator()).readTexture() }
    }

    fun push(identifier: Any, async: Boolean = true, creator: () -> TextureBuffer): DynamicTexture {
        lock.lock()
        cleanup()
        unsafeGet(identifier)?.let { lock.unlock(); return it }

        val index = getNextIndex()

        val texture = createTexture(identifier, index)
        textures[index] = WeakReference(texture)
        texture.state = DynamicTextureState.LOADING

        if (async) {
            DefaultThreadPool += ForcePooledRunnable { texture.load(index, creator) }
        } else {
            texture.load(index, creator)
        }

        lock.unlock()
        return texture
    }


    override fun use(shader: NativeShader, name: String) {
        shaders += shader
        unsafeUse(shader, name)
    }


    private fun getNextIndex(): Int {
        lock.lock()
        for ((index, texture) in textures.withIndex()) {
            if (texture?.get() == null) {
                lock.unlock()
                return index
            }
        }
        val nextIndex = textures.size
        grow()
        lock.unlock()
        return nextIndex
    }

    override fun upload(latch: AbstractLatch?) = upload()

    private fun grow() {
        lock.lock()
        val textures: Array<WeakReference<DynamicTexture>?> = arrayOfNulls(textures.size + initialSize)
        for ((index, texture) in this.textures.withIndex()) {
            if (texture == null) continue
            textures[index] = texture
        }
        this.textures = textures

        this.reload = true
        context.queue += { reload() }
        lock.unlock()
    }

    private fun cleanup() {
        lock.lock()
        for ((index, reference) in textures.withIndex()) {
            if (reference == null) continue
            val texture = reference.get()
            if (texture != null && texture.state != DynamicTextureState.ERROR) continue // not gced yet, keep it for now
            textures[index] = null
        }
        lock.unlock()
    }

    fun reload() {
        unload()
        upload()
        this.reload = false
    }


    protected abstract fun upload(index: Int, texture: DynamicTexture)
    protected abstract fun upload()
    protected abstract fun unload()
    protected abstract fun unsafeUse(shader: NativeShader, name: String = ShaderUniforms.TEXTURES)
    protected abstract fun createTexture(identifier: Any, index: Int): DynamicTexture
}
