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
package de.bixilon.minosoft.data.entities.entities.monster

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions

open class Zombie(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Monster(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val isBaby: Boolean
        get() = data.getBoolean(IS_BABY_DATA, false)

    @get:SynchronizedEntityData
    val specialType: Int
        get() = data.get(SPECIAL_TYPE_DATA, 0)

    @get:SynchronizedEntityData
    val isConvertingToDrowned: Boolean
        get() = data.getBoolean(DROWNING_CONVERSION_DATA, false)


    companion object : EntityFactory<Zombie> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("zombie")
        private val IS_BABY_DATA = EntityDataField("ZOMBIE_IS_BABY")
        private val SPECIAL_TYPE_DATA = EntityDataField("ZOMBIE_SPECIAL_TYPE")
        private val DROWNING_CONVERSION_DATA = EntityDataField("ZOMBIE_DROWNING_CONVERSION")

        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Zombie {
            return Zombie(connection, entityType, data, position, rotation)
        }

        override fun tweak(connection: PlayConnection, data: EntityData?, versionId: Int): ResourceLocation {
            if (data == null || versionId <= ProtocolVersions.V_1_8_9) {
                return RESOURCE_LOCATION
            }
            val specialType = data.get(SPECIAL_TYPE_DATA, 0)
            if (specialType == 1) {
                return ZombieVillager.RESOURCE_LOCATION
            }
            return RESOURCE_LOCATION
        }
    }
}
