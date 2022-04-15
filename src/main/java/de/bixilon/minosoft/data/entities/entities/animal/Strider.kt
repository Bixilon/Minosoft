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
package de.bixilon.minosoft.data.entities.entities.animal

import de.bixilon.minosoft.data.entities.EntityDataFields
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import glm_.vec3.Vec3d

class Strider(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation) : Animal(connection, entityType, position, rotation) {

    @get:SynchronizedEntityData(name = "Boost stime")
    val boostTime: Int
        get() = data.sets.getInt(EntityDataFields.STRIDER_TIME_TO_BOOST)

    @get:SynchronizedEntityData(name = "Is suffocating")
    val isSuffocating: Boolean
        get() = data.sets.getBoolean(EntityDataFields.STRIDER_IS_SUFFOCATING)

    @SynchronizedEntityData(name = "Has saddle")
    val hasSaddle: Boolean
        get() = data.sets.getBoolean(EntityDataFields.STRIDER_HAS_SADDLE)


    companion object : EntityFactory<Strider> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("strider")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation): Strider {
            return Strider(connection, entityType, position, rotation)
        }
    }
}
