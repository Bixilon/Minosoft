/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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
import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil

open class Boat(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Entity(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val timeSinceLastHit: Int
        get() = data.get(TIME_SINCE_LAST_HIT_DATA, 0)

    @get:SynchronizedEntityData
    val hitDirection: Int
        get() = data.get(HIT_DIRECTION_DATA, 1)

    @get:SynchronizedEntityData
    val damageTaken: Float
        get() = data.get(DAMAGE_TAKEN_DATA, 0.0f)

    @get:SynchronizedEntityData
    val material: BoatMaterials
        get() = BoatMaterials.VALUES.getOrNull(data.get(MATERIAL_DATA, BoatMaterials.OAK.ordinal)) ?: BoatMaterials.OAK

    @get:SynchronizedEntityData
    val isLeftPaddleTurning: Boolean
        get() = data.get(PADDLE_LEFT_TURNING_DATA, false)

    @get:SynchronizedEntityData
    val isRightPaddleTurning: Boolean
        get() = data.get(PADDLE_RIGHT_TURNING_DATA, false)

    @get:SynchronizedEntityData
    val splashTimer: Int
        get() = data.get(SPLASH_TIMER_DATA, 0)

    enum class BoatMaterials {
        OAK,
        SPRUCE,
        BIRCH,
        JUNGLE,
        ACACIA,
        DARK_OAK,
        MANGROVE,
        ;

        companion object : ValuesEnum<BoatMaterials> {
            override val VALUES: Array<BoatMaterials> = values()
            override val NAME_MAP: Map<String, BoatMaterials> = EnumUtil.getEnumValues(VALUES)
        }
    }

    companion object : EntityFactory<Boat> {
        override val identifier: ResourceLocation = KUtil.minecraft("boat")
        private val TIME_SINCE_LAST_HIT_DATA = EntityDataField("BOAT_HURT")
        private val HIT_DIRECTION_DATA = EntityDataField("BOAT_HURT_DIRECTION")
        private val DAMAGE_TAKEN_DATA = EntityDataField("BOAT_DAMAGE_TAKEN")
        private val MATERIAL_DATA = EntityDataField("BOAT_MATERIAL")
        private val PADDLE_LEFT_TURNING_DATA = EntityDataField("BOAT_PADDLE_LEFT")
        private val PADDLE_RIGHT_TURNING_DATA = EntityDataField("BOAT_PADDLE_RIGHT")
        private val SPLASH_TIMER_DATA = EntityDataField("BOAT_BUBBLE_TIME")


        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): Boat {
            return Boat(connection, entityType, data, position, rotation)
        }
    }
}
