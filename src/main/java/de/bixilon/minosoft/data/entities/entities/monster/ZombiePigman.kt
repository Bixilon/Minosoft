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
package de.bixilon.minosoft.data.entities.entities.monster

import de.bixilon.minosoft.data.entities.meta.EntityData
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

/**
 * This class is just for the hashmap, it is not used anywhere
 */
@Deprecated("Replaced with ZombifiedPiglin")
class ZombiePigman(connection: PlayConnection, entityType: EntityType) : ZombifiedPiglin(connection, entityType) {

    companion object : EntityFactory<ZombifiedPiglin> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("zombie_pigman")

        override fun tweak(connection: PlayConnection, entityData: EntityData?, versionId: Int): ResourceLocation {
            return ZombifiedPiglin.RESOURCE_LOCATION
        }

        override fun build(connection: PlayConnection, entityType: EntityType): ZombifiedPiglin? {
            return ZombifiedPiglin.build(connection, entityType)
        }
    }
}
