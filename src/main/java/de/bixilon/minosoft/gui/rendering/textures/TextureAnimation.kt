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

package de.bixilon.minosoft.gui.rendering.textures

import de.bixilon.kutil.array.ArrayUtil.isIndex
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.Texture
import de.bixilon.minosoft.gui.rendering.textures.properties.AnimationFrame

class TextureAnimation(
    val animationData: Int,
    val frames: Array<AnimationFrame>,
    val interpolate: Boolean,
    val sprites: Array<Texture>,
) {
    private val totalTime = frames.getTotalTime()
    private var frame = frames.first()
    private var time = 0.0f

    var frame1: Texture = frame.texture
        private set
    var frame2: Texture = frame.next().texture
        private set
    var progress = 0.0f
        private set

    init {
        if (frames.isEmpty() || totalTime <= 0.0f) throw IllegalArgumentException("Invalid texture animation!")
    }

    private fun Array<AnimationFrame>.getTotalTime(): Float {
        var total = 0.0f
        for (frame in this) {
            total += frame.time
        }

        return total
    }


    fun update(delta: Float) {
        val delta = delta % totalTime
        var frame = this.frame
        var left = this.time + delta
        while (left >= frame.time) {
            left -= frame.time
            frame = frame.next()
        }
        this.frame = frame
        this.time = left
        this.frame1 = frame.texture
        this.frame2 = frame.next().texture
        this.progress = if (left == 0.0f) 0.0f else left / frame.time
    }

    private fun AnimationFrame.next(): AnimationFrame {
        var next = index + 1
        if (!frames.isIndex(next)) {
            next = 0
        }
        return frames[next]
    }

}
