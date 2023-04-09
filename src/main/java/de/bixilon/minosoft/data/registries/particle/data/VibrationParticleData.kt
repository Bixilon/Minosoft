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
package de.bixilon.minosoft.data.registries.particle.data

import de.bixilon.minosoft.data.registries.particle.ParticleType
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer

class VibrationParticleData(val source: Any, val arrival: Int, type: ParticleType) : ParticleData(type) {
    override fun toString(): String {
        return "$type: $source in $arrival"
    }

    companion object : ParticleDataFactory<VibrationParticleData> {
        override fun read(buffer: PlayInByteBuffer, type: ParticleType): VibrationParticleData {
            val sourceType = buffer.readResourceLocation()
            val source: Any = when (sourceType.toString()) { // TODO: combine with VibrationS2CP
                "minecraft:block" -> buffer.readBlockPosition()
                "minecraft:entity" -> buffer.readEntityId()
                else -> error("Unknown target type: $sourceType")
            }
            val arrival = buffer.readVarInt()
            return VibrationParticleData(source, arrival, type)
        }
    }
}
