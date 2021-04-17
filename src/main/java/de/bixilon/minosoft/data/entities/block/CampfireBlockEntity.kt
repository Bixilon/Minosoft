/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.entities.block

import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.nbt.tag.CompoundTag
import de.bixilon.minosoft.util.nbt.tag.NBTTag

class CampfireBlockEntity(connection: PlayConnection) : BlockEntity(connection) {
    val items: Array<ItemStack?> = arrayOfNulls(RenderConstants.CAMPFIRE_ITEMS)


    override fun updateNBT(nbt: CompoundTag) {
        val itemArray = nbt.getListTag("Items")?.getValue<NBTTag>() as List<NBTTag>? ?: return
        for (slot in itemArray) {
            check(slot is CompoundTag)

            val itemStack = ItemStack(connection.mapping.itemRegistry.get(slot.getStringTag("id").value)!!, connection.version)

            itemStack.itemCount = slot.getNumberTag("Count").asInt

            items[slot.getNumberTag("Slot").asInt] = itemStack
        }
    }

    companion object : BlockEntityFactory<CampfireBlockEntity> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("minecraft:campfire")

        override fun build(connection: PlayConnection): CampfireBlockEntity {
            return CampfireBlockEntity(connection)
        }
    }
}
