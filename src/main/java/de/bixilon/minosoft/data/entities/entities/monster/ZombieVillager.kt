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
package de.bixilon.minosoft.data.entities.entities.monster

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.entities.entities.npc.villager.data.VillagerData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class ZombieVillager(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Zombie(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val isConverting: Boolean
        get() = data.getBoolean(IS_CONVERTING_DATA, false)

    @get:SynchronizedEntityData
    val villagerData: VillagerData?
        get() = data.get(VILLAGER_DATA_DATA, null) // ToDo: Default villager data


    companion object : EntityFactory<ZombieVillager> {
        override val identifier: ResourceLocation = ResourceLocation("zombie_villager")
        private val IS_CONVERTING_DATA = EntityDataField("ZOMBIE_VILLAGER_IS_CONVERTING")
        private val VILLAGER_DATA_DATA = EntityDataField("ZOMBIE_VILLAGER_DATA")


        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): ZombieVillager {
            return ZombieVillager(connection, entityType, data, position, rotation)
        }
    }
}
