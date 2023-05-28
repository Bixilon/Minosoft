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

package de.bixilon.minosoft.data.world.audio

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.rendering.util.VecUtil.center

interface AbstractAudioPlayer {

    fun playSoundEvent(sound: ResourceLocation, position: Vec3i? = null, volume: Float = 1.0f, pitch: Float = 1.0f) {
        playSound(sound, position?.center, volume, pitch)
    }

    fun playSound(sound: ResourceLocation, position: Vec3d? = null, volume: Float = 1.0f, pitch: Float = 1.0f)

    fun play2DSound(sound: ResourceLocation, volume: Float = 1.0f, pitch: Float = 1.0f) {
        playSound(sound, null as Vec3d?, volume, pitch)
    }

    fun stopAllSounds()

    // ToDo: Stop category
    fun stopSound(sound: ResourceLocation)
}
