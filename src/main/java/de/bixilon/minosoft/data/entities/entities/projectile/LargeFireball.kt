/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.entities.entities.projectile

import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.entities.EntityFactory
import de.bixilon.minosoft.data.mappings.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3

class LargeFireball(connection: PlayConnection, entityType: EntityType, location: Vec3, rotation: EntityRotation) : Fireball(connection, entityType, location, rotation) {
    override val defaultItem: ItemStack
        get() = ItemStack(connection.mapping.itemRegistry[DEFAULT_ITEM]!!, connection.version)

    companion object : EntityFactory<LargeFireball> {
        private val DEFAULT_ITEM = ResourceLocation("fire_charge")
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("fireball")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3, rotation: EntityRotation): LargeFireball {
            return LargeFireball(connection, entityType, position, rotation)
        }
    }
}
