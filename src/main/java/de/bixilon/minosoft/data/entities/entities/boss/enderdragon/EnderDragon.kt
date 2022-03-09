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
package de.bixilon.minosoft.data.entities.entities.boss.enderdragon

import de.bixilon.minosoft.data.entities.EntityDataFields
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.entities.entities.Mob
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class EnderDragon(connection: PlayConnection, entityType: EntityType) : Mob(connection, entityType) {

    @get:EntityMetaDataFunction(name = "Phase")
    val phase: DragonPhases
        get() = DragonPhases.byId(data.sets.getInt(EntityDataFields.ENDER_DRAGON_PHASE))

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

        companion object {
            private val DRAGON_PHASES = values()
            fun byId(id: Int): DragonPhases {
                return DRAGON_PHASES[id]
            }
        }
    }

    companion object : EntityFactory<EnderDragon> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("ender_dragon")

        override fun build(connection: PlayConnection, entityType: EntityType): EnderDragon {
            return EnderDragon(connection, entityType)
        }
    }
}
