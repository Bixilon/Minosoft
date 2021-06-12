/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
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

import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.particle.ParticleType
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions

class BlockParticleData(val blockState: BlockState?, type: ParticleType) : ParticleData(type) {
    override fun toString(): String {
        return "$type: $blockState"
    }

    companion object : ParticleDataFactory<BlockParticleData> {
        override fun read(buffer: PlayInByteBuffer, type: ParticleType): BlockParticleData {
            val blockStateId = if (buffer.versionId < ProtocolVersions.V_17W45A) {
                buffer.readVarInt() shl 4 // ToDo: What about meta data?
            } else {
                buffer.readVarInt()
            }
            return BlockParticleData(buffer.connection.registries.getBlockState(blockStateId), type)
        }
    }
}
