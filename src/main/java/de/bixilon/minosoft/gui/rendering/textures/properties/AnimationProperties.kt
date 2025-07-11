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

package de.bixilon.minosoft.gui.rendering.textures.properties

import com.fasterxml.jackson.annotation.JsonProperty
import de.bixilon.minosoft.data.world.vec.vec2.i.Vec2i
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.protocol.network.session.play.tick.Ticks.Companion.ticks
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import kotlin.time.Duration

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
        val frameTime = this.frameTime.ticks.duration

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
                        frames += Frame(frame["time"].toInt().ticks.duration, frame["index"].toInt())
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
        val time: Duration,
        val texture: Int,
    )
}
