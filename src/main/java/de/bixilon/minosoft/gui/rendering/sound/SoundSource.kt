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

import de.bixilon.minosoft.gui.rendering.sound.sounds.Sound
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import glm_.vec3.Vec3
import org.lwjgl.openal.AL10.*

class SoundSource {
    private var playTime = -1L
    private val source: Int = alGenSources()

    var loop: Boolean = false
        set(value) {
            alSourcei(source, AL_LOOPING, value.AL_VALUE)
            field = value
        }

    var relative: Boolean = false
        set(value) {
            alSourcei(source, AL_SOURCE_RELATIVE, value.AL_VALUE)
            field = value
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

    var sound: Sound? = null
        set(value) {
            stop()
            if (value?.loaded != true || value.loadFailed) {
                field = null
                return
            }
            alSourcei(source, AL_BUFFER, value.buffer)
            field = value
        }

    val isPlaying: Boolean
        get() = alGetSourcei(source, AL_SOURCE_STATE) == AL_PLAYING

    val available: Boolean
        get() = !isPlaying || System.currentTimeMillis() - playTime > (sound?.length ?: 0L) // ToDo: Allow pause

    fun play() {
        playTime = System.currentTimeMillis()
        alSourcePlay(source)
    }

    fun pause() {
        alSourcePause(source)
    }

    fun stop() {
        playTime = -1L
        alSourceStop(source)
    }

    fun unload() {
        stop()
        alDeleteSources(source)
    }

    companion object {
        val Boolean.AL_VALUE: Int
            get() = if (this) {
                AL_TRUE
            } else {
                AL_FALSE
            }
    }

}
