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
package de.bixilon.minosoft.data.entities.entities.projectile

import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.EntityDataFields
import de.bixilon.minosoft.data.entities.entities.EntityMetaDataFunction
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions

class ThrownPotion(connection: PlayConnection, entityType: EntityType) : ThrowableItemProjectile(connection, entityType) {
    override val gravity: Float = 0.05f

    @EntityMetaDataFunction(name = "Item")
    override val item: ItemStack?
        get() = if (versionId > ProtocolVersions.V_20W09A) {
            super.item
        } else {
            data.sets.getItemStack(EntityDataFields.THROWN_POTION_ITEM) ?: defaultItem
        }
    override val defaultItemType: ResourceLocation
        get() = throw NullPointerException()

    override val defaultItem: ItemStack? = null

    companion object : EntityFactory<ThrownPotion> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("potion")

        override fun build(connection: PlayConnection, entityType: EntityType): ThrownPotion {
            return ThrownPotion(connection, entityType)
        }
    }
}
