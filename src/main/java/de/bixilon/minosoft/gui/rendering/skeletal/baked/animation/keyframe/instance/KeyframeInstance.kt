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

package de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.keyframe.instance

import de.bixilon.kutil.time.DurationUtil.rem
import de.bixilon.minosoft.gui.rendering.skeletal.baked.animation.AnimationResult
import de.bixilon.minosoft.gui.rendering.skeletal.instance.TransformInstance
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.AnimationLoops
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animators.keyframes.types.KeyframeData
import java.util.*
import kotlin.time.Duration

abstract class KeyframeInstance<T>(
    private val data: List<KeyframeData<T>>,
    private val loop: AnimationLoops,
) {
    private val length = data.last().time
    private var iterator = data.iterator()

    private var current: T? = null
    private var currentTime = Duration.ZERO
    private var next: T? = null
    private var nextTime = Duration.ZERO

    init {
        val pushed = push() or push() // fill times and values
        if (!pushed) throw IllegalArgumentException("Not enough data points!")
    }


    abstract fun interpolate(delta: Float, previous: T, next: T): T
    abstract fun apply(value: T, transform: TransformInstance)


    fun transform(time: Duration, transform: TransformInstance): AnimationResult {
        if (this.data.size < 2) return AnimationResult.ENDED // must have at least 2 data points
        if (current == null) return AnimationResult.ENDED // illegal state


        when (loop) {
            AnimationLoops.ONCE -> return once(time, transform)
            AnimationLoops.HOLD -> {
                if (once(time, transform) == AnimationResult.ENDED) {
                    apply(current!!, transform)
                }
            }

            AnimationLoops.LOOP -> {
                val time = time % length
                if (time < currentTime || time >= nextTime) restart()
                once(time, transform)
            }
        }
        return AnimationResult.CONTINUE
    }

    private fun once(time: Duration, transform: TransformInstance): AnimationResult {
        if (time < currentTime) return AnimationResult.CONTINUE
        if (currentTime >= nextTime) {
            return AnimationResult.ENDED
        }
        while (time >= nextTime) {
            if (!push()) {
                return AnimationResult.ENDED
            }
        }
        interpolate(time, transform)
        return AnimationResult.CONTINUE
    }

    private fun interpolate(time: Duration, transform: TransformInstance) {
        val length = nextTime - currentTime
        val elapsed = time - currentTime
        val progress = (elapsed / length).toFloat()

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
}
