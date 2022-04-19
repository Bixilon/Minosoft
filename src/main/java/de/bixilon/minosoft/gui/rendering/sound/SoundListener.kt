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

package de.bixilon.minosoft.gui.rendering.sound

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import org.lwjgl.openal.AL10.*

class SoundListener(position: Vec3 = Vec3.EMPTY) {
    var position: Vec3 = position
        set(value) {
            alListener3f(AL_POSITION, value.x, value.y, value.z)
            field = value
        }

    var velocity: Vec3 = Vec3.EMPTY
        set(value) {
            alListener3f(AL_VELOCITY, value.x, value.y, value.z)
            field = value
        }

    var masterVolume: Float
        get() = alGetListenerf(AL_MAX_GAIN)
        set(value) = alListenerf(AL_MAX_GAIN, value)

    fun setOrientation(look: Vec3, up: Vec3) {
        alListenerfv(AL_ORIENTATION, floatArrayOf(look.x, look.y, look.z, up.x, up.y, up.z))
    }

    init {
        this.position = position
        this.velocity = Vec3.EMPTY
        setOrientation(Vec3.EMPTY, Vec3(0.0, 1.0, 0.0))
    }
}
