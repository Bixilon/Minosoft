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

package de.bixilon.minosoft.data.entities.block

import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.nbt.tag.CompoundTag

class MobSpawnerBlockEntity(connection: PlayConnection) : BlockEntity(connection) {


    override fun updateNBT(nbt: CompoundTag) {
        // ToDo: {MaxNearbyEntities: 6s, RequiredPlayerRange: 16s, SpawnCount: 4s, x: -80, y: 4, SpawnData: {id: "minecraft:zombie"}, z: 212, id: "minecraft:mob_spawner", MaxSpawnDelay: 800s, SpawnRange: 4s, Delay: 0s, MinSpawnDelay: 200s}
    }

    companion object : BlockEntityFactory<MobSpawnerBlockEntity> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("minecraft:mob_spawner")

        override fun build(connection: PlayConnection): MobSpawnerBlockEntity {
            return MobSpawnerBlockEntity(connection)
        }
    }
}
