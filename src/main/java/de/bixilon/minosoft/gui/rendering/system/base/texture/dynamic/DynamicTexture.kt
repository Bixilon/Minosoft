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

package de.bixilon.minosoft.gui.rendering.system.base.texture.dynamic

import de.bixilon.minosoft.data.world.vec.vec2.f.Vec2f
import de.bixilon.kutil.concurrent.lock.RWLock
import de.bixilon.kutil.exception.ExceptionUtil.ignoreAll
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.MipmapTextureData
import de.bixilon.minosoft.gui.rendering.system.base.texture.shader.ShaderTexture

abstract class DynamicTexture(
    val identifier: Any,
) : ShaderTexture {
    private val callbacks: MutableSet<DynamicTextureListener> = mutableSetOf()
    private val lock = RWLock.rwlock()

    var data: MipmapTextureData? = null
    var state: DynamicTextureState = DynamicTextureState.WAITING
        set(value) {
            if (field == value) {
                return
            }
            field = value
            lock.acquire()
            val iterator = callbacks.iterator()
            for (callback in iterator) {
                val remove = ignoreAll { callback.onDynamicTextureChange(this) } ?: continue
                if (remove) {
                    iterator.remove()
                }
            }
            lock.release()
        }

    override fun toString(): String {
        return identifier.toString()
    }

    override fun transformUV(end: Vec2f?): Vec2f {
        return end ?: Vec2f(1.0f)  // TODO: memory
    }

    override fun transformUV(end: FloatArray?): FloatArray {
        return end ?: floatArrayOf(1.0f, 1.0f) // TODO: memory
    }

    operator fun plusAssign(callback: DynamicTextureListener) = addListener(callback)
    fun addListener(callback: DynamicTextureListener) {
        lock.lock()
        callbacks += callback
        lock.unlock()
    }

    operator fun minusAssign(callback: DynamicTextureListener) = removeListener(callback)
    fun removeListener(callback: DynamicTextureListener) {
        lock.lock()
        callbacks -= callback
        lock.unlock()
    }
}
