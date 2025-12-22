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

package de.bixilon.minosoft.gui.rendering.system.base.texture.animator

import de.bixilon.kutil.array.ArrayUtil.isIndex
import de.bixilon.kutil.time.DurationUtil.rem
import de.bixilon.minosoft.gui.rendering.system.base.texture.data.buffer.TextureBuffer
import kotlin.time.Duration

class TextureAnimation(
    val frames: Array<AnimationFrame>,
    val interpolate: Boolean,
    val sprites: Array<TextureBuffer>,
) {
    private val totalTime = frames.getTotalTime()
    private var frame = frames.first()
    private var time = Duration.ZERO

    var frame1: TextureBuffer = frame.buffer
        private set
    var frame2: TextureBuffer = frame.next().buffer
        private set
    var progress = 0.0f
        private set

    init {
        assert(frames.isNotEmpty() && totalTime > Duration.ZERO) { "Invalid texture animation!" }
    }

    private fun Array<AnimationFrame>.getTotalTime(): Duration {
        var total = Duration.ZERO
        for (frame in this) {
            total += frame.time
        }

        return total
    }


    fun update(delta: Duration) {
        val delta = delta % totalTime
        var frame = this.frame
        var left = this.time + delta
        while (left >= frame.time) {
            left -= frame.time
            frame = frame.next()
        }
        this.frame = frame
        this.time = left
        this.frame1 = frame.buffer
        this.frame2 = frame.next().buffer
        this.progress = if (left == Duration.ZERO) 0.0f else (left / frame.time).toFloat()
    }

    private fun AnimationFrame.next(): AnimationFrame {
        var next = index + 1
        if (!frames.isIndex(next)) {
            next = 0
        }
        return frames[next]
    }
}
