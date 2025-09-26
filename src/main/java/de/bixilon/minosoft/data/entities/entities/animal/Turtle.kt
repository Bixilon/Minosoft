/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

class Turtle(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Animal(session, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val homePosition: BlockPosition?
        get() = data.get(HOME_POSITION_DATA, null)

    @get:SynchronizedEntityData
    val hasEgg: Boolean
        get() = data.getBoolean(HAS_EGG_DATA, false)

    @get:SynchronizedEntityData
    val isLayingEgg: Boolean
        get() = data.getBoolean(IS_LAYING_EGG_DATA, false)

    @get:SynchronizedEntityData
    val travelPosition: BlockPosition?
        get() = data.get(TRAVEL_POSITION_DATA, null)

    @get:SynchronizedEntityData
    val isGoingHome: Boolean
        get() = data.getBoolean(IS_GOING_HOME_DATA, false)

    @get:SynchronizedEntityData
    val isTraveling: Boolean
        get() = data.getBoolean(IS_TRAVELING_DATA, false)


    companion object : EntityFactory<Turtle> {
        override val identifier: ResourceLocation = minecraft("turtle")
        private val HOME_POSITION_DATA = EntityDataField("TURTLE_HOME_POSITION")
        private val HAS_EGG_DATA = EntityDataField("TURTLE_HAS_EGG")
        private val IS_LAYING_EGG_DATA = EntityDataField("TURTLE_IS_LAYING_EGG")
        private val TRAVEL_POSITION_DATA = EntityDataField("TURTLE_TRAVEL_POSITION")
        private val IS_GOING_HOME_DATA = EntityDataField("TURTLE_IS_GOING_HOME")
        private val IS_TRAVELING_DATA = EntityDataField("TURTLE_IS_TRAVELING")


        override fun build(session: PlaySession, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Turtle {
            return Turtle(session, entityType, data, position, rotation)
        }
    }
}
