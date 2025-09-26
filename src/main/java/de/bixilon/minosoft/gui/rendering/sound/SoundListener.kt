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

package de.bixilon.minosoft.gui.rendering.sound

import de.bixilon.kmath.vec.vec3.f.Vec3f
import org.lwjgl.openal.AL10.*

class SoundListener(position: Vec3f = Vec3f.EMPTY) {
    var position: Vec3f = position
        set(value) {
            alListener3f(AL_POSITION, value.x, value.y, value.z)
            field = value
        }

    var velocity: Vec3f = Vec3f.EMPTY
        set(value) {
            alListener3f(AL_VELOCITY, value.x, value.y, value.z)
            field = value
        }

    var masterVolume: Float
        get() = alGetListenerf(AL_MAX_GAIN)
        set(value) = alListenerf(AL_MAX_GAIN, value)

    fun setOrientation(look: Vec3f, up: Vec3f) {
        alListenerfv(AL_ORIENTATION, floatArrayOf(look.x, look.y, look.z, up.x, up.y, up.z))
    }

    init {
        this.position = position
        this.velocity = Vec3f.EMPTY
        setOrientation(Vec3f.EMPTY, Vec3f(0.0f, 1.0f, 0.0f))
    }
}
