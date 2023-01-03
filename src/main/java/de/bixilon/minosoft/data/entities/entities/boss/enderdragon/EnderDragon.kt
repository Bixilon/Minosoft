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
package de.bixilon.minosoft.data.entities.entities.boss.enderdragon

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.entities.entities.Mob
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil

class EnderDragon(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : Mob(connection, entityType, data, position, rotation) {

    @get:SynchronizedEntityData
    val phase: DragonPhases
        get() = DragonPhases.VALUES.getOrNull(data.get(PHASE_DATA, DragonPhases.HOVER.ordinal)) ?: DragonPhases.HOVER

    enum class DragonPhases {
        HOLDING,
        STRAFING,
        LANDING_APPROACH,
        LANDING,
        TAKEOFF,
        SITTING_FLAMING,
        SITTING_SCANNING,
        SITTING_ATTACKING,
        CHARGE_PLAYER,
        DEATH,
        HOVER,
        ;

        companion object : ValuesEnum<DragonPhases> {
            override val VALUES: Array<DragonPhases> = values()
            override val NAME_MAP: Map<String, DragonPhases> = EnumUtil.getEnumValues(VALUES)
        }
    }

    companion object : EntityFactory<EnderDragon> {
        override val identifier: ResourceLocation = KUtil.minecraft("ender_dragon")
        private val PHASE_DATA = EntityDataField("ENDER_DRAGON_PHASE")


        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): EnderDragon {
            return EnderDragon(connection, entityType, data, position, rotation)
        }
    }
}
