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

package de.bixilon.minosoft.data.entities.data.types

import de.bixilon.minosoft.data.entities.data.types.registry.RegistryEntityDataType
import de.bixilon.minosoft.data.entities.entities.npc.villager.data.VillagerData
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer

object VillagerDataEntityDataType : RegistryEntityDataType<VillagerData> {

    override fun read(buffer: PlayInByteBuffer): VillagerData? {
        val type = read(buffer, buffer.connection.registries.villagerType) ?: return null
        val profession = read(buffer, buffer.connection.registries.villagerProfession) ?: return null
        val level = buffer.readVarInt()
        return VillagerData(type, profession, level)
    }
}
