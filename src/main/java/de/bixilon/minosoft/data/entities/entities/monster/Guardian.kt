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
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions

open class Guardian(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Monster(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val isMoving: Boolean
        get() = data.getBoolean(IS_MOVING_DATA, false)

    @get:SynchronizedEntityData
    val _target: Int?
        get() = data.get(TARGET_DATA, null)

    val target: Entity?
        get() = connection.world.entities[_target]


    companion object : EntityFactory<Guardian> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("guardian")
        private val IS_MOVING_DATA = EntityDataField("GUARDIAN_IS_MOVING")
        private val TARGET_DATA = EntityDataField("GUARDIAN_TARGET_ENTITY_ID")
        private val LEGACY_FLAGS_DATA = EntityDataField("LEGACY_GUARDIAN_FLAGS")


        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Guardian {
            return Guardian(connection, entityType, data, position, rotation)
        }

        override fun tweak(connection: PlayConnection, data: EntityData?, versionId: Int): ResourceLocation {
            if (data == null || versionId <= ProtocolVersions.V_1_8_9) {
                return RESOURCE_LOCATION
            }
            val specialType = data.getBitMask(LEGACY_FLAGS_DATA, 0x02, 0)
            if (specialType) {
                return ElderGuardian.RESOURCE_LOCATION
            }
            return RESOURCE_LOCATION
        }
    }
}
