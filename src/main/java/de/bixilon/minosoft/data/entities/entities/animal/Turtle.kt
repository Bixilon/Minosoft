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
import glm_.vec3.Vec3i

class Turtle(connection: PlayConnection, entityType: EntityType, position: Vec3, rotation: EntityRotation) : Animal(connection, entityType, position, rotation) {

    @get:EntityMetaDataFunction(name = "Home Position")
    val homePosition: Vec3i?
        get() = entityMetaData.sets.getBlockPosition(EntityMetaDataFields.TURTLE_HOME_POSITION)

    @EntityMetaDataFunction(name = "Has egg")
    val hasEgg: Boolean
        get() = entityMetaData.sets.getBoolean(EntityMetaDataFields.TURTLE_HAS_EGG)

    @get:EntityMetaDataFunction(name = "Is laying egg")
    val isLayingEgg: Boolean
        get() = entityMetaData.sets.getBoolean(EntityMetaDataFields.TURTLE_IS_LAYING_EGG)

    @get:EntityMetaDataFunction(name = "Travel position")
    val travelPosition: Vec3i?
        get() = entityMetaData.sets.getBlockPosition(EntityMetaDataFields.TURTLE_TRAVEL_POSITION)

    @get:EntityMetaDataFunction(name = "Is going home")
    val isGoingHome: Boolean
        get() = entityMetaData.sets.getBoolean(EntityMetaDataFields.TURTLE_IS_GOING_HOME)

    @get:EntityMetaDataFunction(name = "Is traveling")
    val isTraveling: Boolean
        get() = entityMetaData.sets.getBoolean(EntityMetaDataFields.TURTLE_IS_TRAVELING)


    companion object : EntityFactory<Turtle> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("turtle")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3, rotation: EntityRotation): Turtle {
            return Turtle(connection, entityType, position, rotation)
        }
    }
}
