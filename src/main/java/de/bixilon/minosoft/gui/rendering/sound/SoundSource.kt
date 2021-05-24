/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.sound

import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import glm_.vec3.Vec3
import org.lwjgl.openal.AL10.*

class SoundSource(loop: Boolean = false) {
    private val source: Int = alGenSources()

    init {
        if (loop) {
            alSourcei(source, AL_LOOPING, AL_TRUE)
        } else {
            alSourcei(source, AL_SOURCE_RELATIVE, AL_TRUE)
        }
    }

    var position: Vec3 = Vec3.EMPTY
        set(value) {
            alSource3f(source, AL_POSITION, value.x, value.y, value.z)
            field = value
        }

    var velocity: Vec3 = Vec3.EMPTY
        set(value) {
            alSource3f(source, AL_VELOCITY, value.x, value.y, value.z)
            field = value
        }

    var gain: Float = 1.0f
        set(value) {
            alSourcef(source, AL_GAIN, value)
            field = value
        }

    var pitch: Float = 1.0f
        set(value) {
            alSourcef(source, AL_PITCH, value)
            field = value
        }

    var buffer: Int = -1
        set(value) {
            alSourcei(source, AL_BUFFER, value)
            field = value
        }

    val isPlaying: Boolean
        get() = alGetSourcei(source, AL_SOURCE_STATE) == AL_PLAYING

    fun play() {
        alSourcePlay(source)
    }

    fun pause() {
        alSourcePause(source)
    }

    fun stop() {
        alSourceStop(source)
    }

    fun delete() {
        stop()
        alDeleteSources(source)
    }

}
