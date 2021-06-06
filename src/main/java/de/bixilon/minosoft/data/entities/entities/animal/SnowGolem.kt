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
package de.bixilon.minosoft.data.entities.entities.animal

import de.bixilon.minosoft.data.entities.EntityMetaDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.entities.EntityFactory
import de.bixilon.minosoft.data.mappings.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3d

class SnowGolem(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : AbstractGolem(connection, entityType, position, rotation) {

    private fun getPumpkinFlags(bitMask: Int): Boolean {
        return entityMetaData.sets.getBitMask(EntityMetaDataFields.SNOW_GOLEM_FLAGS, bitMask)
    }

    @EntityMetaDataFunction(name = "Pumpkin hat")
    fun hasPumpkinHat(): Boolean {
        return getPumpkinFlags(0x10)
    }

    companion object : EntityFactory<SnowGolem> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("snow_golem")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation): SnowGolem {
            return SnowGolem(connection, entityType, position, rotation)
        }
    }
}
