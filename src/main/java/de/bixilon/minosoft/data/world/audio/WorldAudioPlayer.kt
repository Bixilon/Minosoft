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
import de.bixilon.minosoft.data.registries.identified.ResourceLocation

@Deprecated("use world.audio")
interface WorldAudioPlayer : AbstractAudioPlayer {
    val audio: AbstractAudioPlayer?

    override fun playSound(sound: ResourceLocation, position: Vec3d?, volume: Float, pitch: Float) {
        audio?.playSound(sound, position, volume, pitch)
    }

    override fun stopAllSounds() {
        audio?.stopAllSounds()
    }

    override fun stopSound(sound: ResourceLocation) {
        audio?.stopSound(sound)
    }
}
