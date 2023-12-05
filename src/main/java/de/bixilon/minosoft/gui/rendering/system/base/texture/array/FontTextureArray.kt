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

import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.pool.ThreadPool.Priorities.HIGH
import de.bixilon.kutil.concurrent.pool.runnable.ForcePooledRunnable
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.texture.TextureStates
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture

abstract class FontTextureArray(
    val context: RenderContext,
    val resolution: Int,
    val compressed: Boolean,
) : TextureArray {
    protected val textures: MutableSet<Texture> = mutableSetOf()
    private val lock = SimpleLock()
    var state: TextureArrayStates = TextureArrayStates.DECLARED
        protected set

    operator fun plusAssign(texture: Texture) = push(texture)

    fun push(texture: Texture) {
        if (texture.state != TextureStates.LOADED) {
            DefaultThreadPool += ForcePooledRunnable(priority = HIGH) { texture.load(context) }
        }
        lock.lock()
        textures += texture
        lock.unlock()
    }

    abstract fun load(latch: AbstractLatch)
}
