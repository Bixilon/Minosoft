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
package de.bixilon.minosoft.data.entities.entities.monster.raid

import de.bixilon.minosoft.data.entities.EntityDataFields
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class Witch(connection: PlayConnection, entityType: EntityType) : Raider(connection, entityType) {

    @get:EntityMetaDataFunction(name = "Is drinking Potion")
    val isDrinkingPotion: Boolean
        get() = data.sets.getBoolean(EntityDataFields.WITCH_IS_DRINKING_POTION)

    @get:EntityMetaDataFunction(name = "Is aggressive")
    override val isAggressive: Boolean
        get() = data.sets.getBoolean(EntityDataFields.LEGACY_WITCH_IS_AGGRESSIVE)

    companion object : EntityFactory<Witch> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("witch")

        override fun build(connection: PlayConnection, entityType: EntityType): Witch {
            return Witch(connection, entityType)
        }
    }
}
