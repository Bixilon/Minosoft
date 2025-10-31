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

package de.bixilon.minosoft.data.world.audio

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.BlockPositionUtil.center

interface AbstractAudioPlayer {

    fun play(sound: ResourceLocation, position: BlockPosition? = null, volume: Float = 1.0f, pitch: Float = 1.0f) = play(sound, position?.center, volume, pitch)

    fun play(sound: ResourceLocation, position: Vec3d? = null, volume: Float = 1.0f, pitch: Float = 1.0f)

    fun play2D(sound: ResourceLocation, volume: Float = 1.0f, pitch: Float = 1.0f) {
        play(sound, null as Vec3d?, volume, pitch)
    }

    fun stopAll()

    // ToDo: Stop category
    fun stop(sound: ResourceLocation)
}
