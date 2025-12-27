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

import de.bixilon.kutil.time.DurationUtil.rem
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import kotlin.time.Duration

class TextureAnimation(
    val texture: Texture,
    val frames: Array<AnimationFrame>,
    val interpolate: Boolean,
) {
    private val total = frames.sumOf { it.time }
    private var time = Duration.ZERO

    var frame: AnimationFrame = frames.first()
        private set
    var progress = 0.0f
        private set


    init {
        assert(frames.isNotEmpty() && total > Duration.ZERO) { "Invalid texture animation!" }
    }

    @Deprecated("kutil 1.30.3")
    inline fun <E> Array<E>.sumOf(selector: (E) -> Duration): Duration {
        var sum = Duration.ZERO
        for (element in this) {
            sum += selector.invoke(element)
        }

        return sum
    }

    fun update(delta: Duration) {
        val delta = delta % total
        var left = this.time + delta
        while (left >= frame.time) {
            left -= frame.time
            frame = frame.next
        }
        this.time = left
        this.progress = if (left == Duration.ZERO) 0.0f else (left / frame.time).toFloat()
    }
}
