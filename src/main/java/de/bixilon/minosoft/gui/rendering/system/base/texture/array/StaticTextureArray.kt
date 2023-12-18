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

package de.bixilon.minosoft.gui.rendering.system.base.texture.array

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPool
import de.bixilon.kutil.concurrent.pool.runnable.ForcePooledRunnable
import de.bixilon.kutil.concurrent.pool.runnable.SimplePoolRunnable
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.latch.AbstractLatch.Companion.child
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.sprite.SpriteAnimator
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.file.PNGTexture

abstract class StaticTextureArray(
    val context: RenderContext,
    val async: Boolean,
    val mipmaps: Int,
) : TextureArray {
    protected val named: MutableMap<ResourceLocation, Texture> = mutableMapOf()
    protected val other: MutableSet<Texture> = mutableSetOf()
    private val lock = SimpleLock()

    val animator = SpriteAnimator(context)
    var state: TextureArrayStates = TextureArrayStates.DECLARED
        protected set


    operator fun get(resourceLocation: ResourceLocation): Texture? {
        val state = state
        if (state != TextureArrayStates.UPLOADED) {
            lock.acquire()
        }
        val texture = this.named[resourceLocation]
        if (state != TextureArrayStates.UPLOADED) {
            lock.release()
        }
        return texture
    }

    operator fun plusAssign(texture: Texture) = push(texture)

    fun push(texture: Texture) {
        lock.lock()
        other += texture
        lock.unlock()
        if (texture.state != TextureStates.LOADED && async) {
            DefaultThreadPool += ForcePooledRunnable { texture.load(context) }
        }
    }

    open fun create(resourceLocation: ResourceLocation, mipmaps: Boolean = true, factory: (mipmaps: Int) -> Texture = { PNGTexture(resourceLocation, mipmaps = it) }): Texture {
        lock.lock()
        named[resourceLocation]?.let { lock.unlock(); return it }
        val texture = factory.invoke(if (mipmaps) this.mipmaps else 0)

        named[resourceLocation] = texture
        lock.unlock()
        if (async) {
            DefaultThreadPool += ForcePooledRunnable { texture.load(context) }
        }

        return texture
    }

    abstract fun findResolution(size: Vec2i): Vec2i


    private fun load(latch: AbstractLatch, textures: Collection<Texture>) {
        for (texture in textures) {
            if (texture.state != TextureStates.DECLARED) continue

            latch.inc()
            DefaultThreadPool += SimplePoolRunnable(ThreadPool.HIGH) { texture.load(context); latch.dec() }
        }
    }

    protected abstract fun load(textures: Collection<Texture>)

    fun load(latch: AbstractLatch) {
        val latch = latch.child(0)
        load(latch, named.values)
        load(latch, other)
        latch.await()

        load(named.values)
        load(other)

        state = TextureArrayStates.LOADED
    }
}
