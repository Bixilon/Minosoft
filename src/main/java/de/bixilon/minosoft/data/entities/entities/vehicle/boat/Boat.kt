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
package de.bixilon.minosoft.data.entities.entities.vehicle.boat

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

open class Boat(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : Entity(connection, entityType, position, rotation) {

    @get:SynchronizedEntityData(name = "Time since last hit")
    val timeSinceLastHit: Int
        get() = data.sets.getInt(EntityDataFields.BOAT_HURT)

    @get:SynchronizedEntityData(name = "Forward direction")
    val forwardDirection: Int
        get() = data.sets.getInt(EntityDataFields.BOAT_HURT_DIRECTION)

    @get:SynchronizedEntityData(name = "Damage taken")
    val damageTaken: Float
        get() = data.sets.getFloat(EntityDataFields.BOAT_DAMAGE_TAKEN)

    @get:SynchronizedEntityData(name = "Material")
    val material: BoatMaterials
        get() = BoatMaterials.byId(data.sets.getInt(EntityDataFields.BOAT_MATERIAL))

    @get:SynchronizedEntityData(name = "Left paddle turning")
    val isLeftPaddleTurning: Boolean
        get() = data.sets.getBoolean(EntityDataFields.BOAT_PADDLE_LEFT)

    @get:SynchronizedEntityData(name = "Right paddle turning")
    val isRightPaddleTurning: Boolean
        get() = data.sets.getBoolean(EntityDataFields.BOAT_PADDLE_RIGHT)

    @get:SynchronizedEntityData(name = "Splash timer")
    val splashTimer: Int
        get() = data.sets.getInt(EntityDataFields.BOAT_BUBBLE_TIME)

    enum class BoatMaterials {
        OAK,
        SPRUCE,
        BIRCH,
        JUNGLE,
        ACACIA,
        DARK_OAK,
        ;

        companion object {
            private val BOAT_MATERIALS = values()

            fun byId(id: Int): BoatMaterials {
                return BOAT_MATERIALS[id]
            }
        }
    }

    companion object : EntityFactory<Boat> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("boat")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation): Boat {
            return Boat(connection, entityType, position, rotation)
        }
    }
}
