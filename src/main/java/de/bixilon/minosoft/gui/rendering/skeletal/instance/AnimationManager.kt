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

package de.bixilon.minosoft.gui.rendering.skeletal.instance

import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.AbstractAnimation
import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.instance.KeyframeInstance.Companion.OVER

class AnimationManager(val instance: SkeletalInstance) {
    private val playing: MutableMap<String, AbstractAnimation> = mutableMapOf()
    private val lock = SimpleLock()
    private var lastDraw = -1L


    fun play(animation: AbstractAnimation) {
        lock.lock()
        playing[animation.name] = animation
        lock.unlock()
    }

    fun play(name: String) {
        val animation = instance.model.animations[name] ?: throw IllegalArgumentException("Can not find animation $name!")
        play(animation.instance(name, instance))
    }

    fun stop(animation: AbstractAnimation) {
        stop(animation.name)
    }

    fun stop(name: String) {
        lock.lock()
        playing -= name
        lock.unlock()
    }


    fun draw() {
        val millis = millis()
        val delta = if (lastDraw < 0) 0L else millis - lastDraw
        this.lastDraw = millis
        draw(delta / 1000.0f)
    }

    fun reset() {
        lock.lock()
        playing.clear()
        instance.transform.reset()
        lock.unlock()
    }

    fun draw(delta: Float) {
        lock.lock()
        val iterator = playing.iterator()

        for ((name, animation) in iterator) {
            val over = animation.draw(delta)
            if (over == OVER) {
                iterator.remove()
            }
        }
        lock.unlock()
    }
}
