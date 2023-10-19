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

package de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.instance

import de.bixilon.minosoft.gui.rendering.skeletal.instance.TransformInstance
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.AnimationLoops
import java.util.*

abstract class KeyframeInstance<T>(
    private val data: SortedMap<Float, T>,
    private val loop: AnimationLoops,
) {
    private val length = data.lastKey()
    private var iterator = data.iterator()

    private var current: T? = null
    private var currentTime = 0.0f
    private var next: T? = null
    private var nextTime = 0.0f

    init {
        val pushed = push() or push() // fill times and values
        if (!pushed) throw IllegalArgumentException("Not enough data points!")
    }


    abstract fun interpolate(delta: Float, previous: T, next: T): T
    abstract fun apply(value: T, transform: TransformInstance)


    fun transform(time: Float, transform: TransformInstance): Boolean {
        if (this.data.size < 2) return OVER // must have at least 2 data points
        if (current == null) return OVER // illegal state


        when (loop) {
            AnimationLoops.ONCE -> return once(time, transform)
            AnimationLoops.HOLD -> {
                if (once(time, transform) == OVER) {
                    apply(current!!, transform)
                }
            }

            AnimationLoops.LOOP -> {
                val time = time % length
                if (time < currentTime || time >= nextTime) restart()
                once(time, transform)
            }
        }
        return NOT_OVER
    }

    private fun once(time: Float, transform: TransformInstance): Boolean {
        if (time < currentTime) return NOT_OVER
        if (currentTime >= nextTime) {
            return OVER
        }
        while (time >= nextTime) {
            if (!push()) {
                return OVER
            }
        }
        interpolate(time, transform)
        return NOT_OVER
    }

    private fun interpolate(time: Float, transform: TransformInstance) {
        val length = nextTime - currentTime
        val elapsed = time - currentTime
        val progress = elapsed / length

        val value = interpolate(progress, current!!, next!!)
        apply(value, transform)
    }

    fun restart() {
        this.iterator = data.iterator()
        push(); push()
    }

    private fun push(): Boolean {
        this.current = this.next
        this.currentTime = this.nextTime

        if (!iterator.hasNext()) return false
        val (nextTime, next) = iterator.next()

        this.nextTime = nextTime
        this.next = next

        return true
    }

    companion object {
        const val OVER = true
        const val NOT_OVER = !OVER
    }
}
