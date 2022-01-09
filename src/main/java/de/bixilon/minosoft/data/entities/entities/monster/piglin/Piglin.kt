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
package de.bixilon.minosoft.data.entities.entities.monster.piglin

import de.bixilon.minosoft.data.entities.EntityDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import glm_.vec3.Vec3d

class Piglin(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : AbstractPiglin(connection, entityType, position, rotation) {

    @EntityMetaDataFunction(name = "Is immune to zombification")
    override val isImmuneToZombification: Boolean
        get() = if (versionId < ProtocolVersions.V_20W27A) {
            super.isImmuneToZombification
        } else {
            data.sets.getBoolean(EntityDataFields.PIGLIN_IMMUNE_TO_ZOMBIFICATION)

        }

    @get:EntityMetaDataFunction(name = "Is baby")
    val isBaby: Boolean
        get() = data.sets.getBoolean(EntityDataFields.PIGLIN_IS_BABY)

    @get:EntityMetaDataFunction(name = "Is charging crossbow")
    val isChargingCrossbow: Boolean
        get() = data.sets.getBoolean(EntityDataFields.PIGLIN_IS_CHARGING_CROSSBOW)

    @get:EntityMetaDataFunction(name = "Is dancing")
    val isDancing: Boolean
        get() = data.sets.getBoolean(EntityDataFields.PIGLIN_IS_DANCING)


    companion object : EntityFactory<Piglin> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("piglin")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation): Piglin {
            return Piglin(connection, entityType, position, rotation)
        }
    }
}
