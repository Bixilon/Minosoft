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

class Bee(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : Animal(connection, entityType, position, rotation) {

    private fun getBeeFlag(bitMask: Int): Boolean {
        return entityMetaData.sets.getBitMask(EntityMetaDataFields.BEE_FLAGS, bitMask)
    }

    @get:EntityMetaDataFunction(name = "Is angry")
    val isAngry: Boolean
        get() = getBeeFlag(0x02)

    @EntityMetaDataFunction(name = "Has stung")
    fun hasStung(): Boolean {
        return getBeeFlag(0x04)
    }

    @EntityMetaDataFunction(name = "has Nectar")
    fun hasNectar(): Boolean {
        return getBeeFlag(0x08)
    }

    @get:EntityMetaDataFunction(name = "Remaining anger time")
    val remainingAngerTimer: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.BEE_REMAINING_ANGER_TIME)


    companion object : EntityFactory<Bee> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("bee")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation): Bee {
            return Bee(connection, entityType, position, rotation)
        }
    }
}
