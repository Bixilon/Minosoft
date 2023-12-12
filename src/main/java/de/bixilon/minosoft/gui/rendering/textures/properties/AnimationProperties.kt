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

package de.bixilon.minosoft.gui.rendering.textures.properties

import com.fasterxml.jackson.annotation.JsonProperty
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

data class AnimationProperties(
    val interpolate: Boolean = false,
    val width: Int = -1,
    val height: Int = -1,
    @JsonProperty("frametime") val frameTime: Int = 1,
    val frames: List<Any> = emptyList(),
) {


    fun create(size: Vec2i): FrameData {
        val width = size.x
        val height = if (height <= 0) size.x else height // they are squares?
        val count = size.y / height

        val frames: MutableList<Frame> = mutableListOf()
        val frameTime = ticksToSeconds(this.frameTime)

        if (this.frames.isEmpty()) {
            // automatic
            for (i in 0 until count) {
                frames += Frame(frameTime, i)
            }
        } else {
            for (frame in this.frames) {
                when (frame) {
                    is Number -> frames += Frame(frameTime, frame.toInt())
                    is Map<*, *> -> {
                        frames += Frame(ticksToSeconds(frame["time"].toInt()), frame["index"].toInt())
                    }
                }
            }
        }

        return FrameData(frames, count, Vec2i(width, height))
    }

    data class FrameData(
        val frames: List<Frame>,
        val textures: Int,
        val size: Vec2i,
    )

    data class Frame(
        val time: Float,
        val texture: Int,
    )

    companion object {

        private fun ticksToSeconds(ticks: Int): Float {
            val millis = ticks * ProtocolDefinition.TICK_TIME
            return millis / 1000.0f
        }
    }
}
