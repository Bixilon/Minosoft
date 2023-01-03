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

import de.bixilon.minosoft.data.container.ItemStackUtil
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.particle.ParticleType
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions

class ItemParticleData(val itemStack: ItemStack?, type: ParticleType) : ParticleData(type) {

    override fun toString(): String {
        return "$type: $itemStack"
    }

    companion object : ParticleDataFactory<ItemParticleData> {
        override fun read(buffer: PlayInByteBuffer, type: ParticleType): ItemParticleData {
            val itemId = if (buffer.versionId < ProtocolVersions.V_17W45A) {
                buffer.readVarInt() shl 16 or buffer.readVarInt()
            } else {
                buffer.readVarInt()
            }
            return ItemParticleData(ItemStackUtil.of(buffer.connection.registries.item[itemId], connection = buffer.connection), type)
        }
    }
}
