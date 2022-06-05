/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.gui.rendering.system.base.texture.texture.AbstractTexture

data class AnimationProperties(
    val interpolate: Boolean = false,
    var width: Int = -1,
    var height: Int = -1,
    @JsonProperty("frametime") private val frameTime: Int = 1,
    @JsonProperty("frames") private val _frames: List<Any> = emptyList(),
) {
    @JsonIgnore
    private var initialized = false

    @JsonIgnore
    lateinit var frames: Array<AnimationFrame>
        private set

    @JsonIgnore
    var frameCount = -1
        private set

    fun postInit(texture: AbstractTexture) {
        if (initialized) {
            error("")
        }
        if (width == -1) {
            width = texture.size.x
        }
        if (height == -1) {
            height = texture.size.x // That's correct!
        }

        frameCount = texture.size.y / height

        val frames: MutableList<AnimationFrame> = mutableListOf()

        if (_frames.isEmpty()) {
            for (i in 0 until frameCount) {
                frames += AnimationFrame(i, frameTime)
            }
        } else {
            for (frame in _frames) {
                if (frame is Number) {
                    frames += AnimationFrame(frame.toInt(), frameTime)
                    continue
                }
                check(frame is Map<*, *>) { "Invalid frame: $frame" }

                frames += AnimationFrame(frame["index"]!!.toInt(), frame["time"]?.toInt() ?: frameTime)
            }
        }

        this.frames = frames.toTypedArray()
        initialized = true
    }
}
