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
import glm_.vec3.Vec3
import java.util.*

class Fox(connection: PlayConnection, entityType: EntityType, position: Vec3, rotation: EntityRotation) : Animal(connection, entityType, position, rotation) {

    @get:EntityMetaDataFunction(name = "Variant")
    val variant: Int
        get() = entityMetaData.sets.getInt(EntityMetaDataFields.FOX_VARIANT)

    private fun getFoxFlag(bitMask: Int): Boolean {
        return entityMetaData.sets.getBitMask(EntityMetaDataFields.FOX_FLAGS, bitMask)
    }

    @get:EntityMetaDataFunction(name = "Is sitting")
    val isSitting: Boolean
        get() = getFoxFlag(0x01)

    @get:EntityMetaDataFunction(name = "Is crouching")
    override val isSneaking: Boolean
        get() = getFoxFlag(0x04)

    @get:EntityMetaDataFunction(name = "Is interested")
    val isInterested: Boolean
        get() = getFoxFlag(0x08)

    @get:EntityMetaDataFunction(name = "Is pouncing")
    val isPouncing: Boolean
        get() = getFoxFlag(0x10)

    @get:EntityMetaDataFunction(name = "Is sleeping")
    override val isSleeping: Boolean
        get() = getFoxFlag(0x20)

    @get:EntityMetaDataFunction(name = "Is faceplanted")
    val isFaceplanted: Boolean
        get() = getFoxFlag(0x40)

    @get:EntityMetaDataFunction(name = "Is defending")
    val isDefending: Boolean
        get() = getFoxFlag(0x80)

    @get:EntityMetaDataFunction(name = "Trusted 1")
    val firstTrusted: UUID?
        get() = entityMetaData.sets.getUUID(EntityMetaDataFields.FOX_TRUSTED_1)

    @get:EntityMetaDataFunction(name = "Trusted 2")
    val secondTrusted: UUID?
        get() = entityMetaData.sets.getUUID(EntityMetaDataFields.FOX_TRUSTED_2)


    companion object : EntityFactory<Fox> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("fox")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3, rotation: EntityRotation): Fox {
            return Fox(connection, entityType, position, rotation)
        }
    }
}
