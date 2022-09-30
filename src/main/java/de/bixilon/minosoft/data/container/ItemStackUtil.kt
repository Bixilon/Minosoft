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

package de.bixilon.minosoft.data.container

import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.container.stack.property.HolderProperty
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

object ItemStackUtil {

    fun of(item: Item, connection: PlayConnection, nbt: JsonObject): ItemStack {
        TODO()
    }

    fun of(
        item: Item,
        count: Int = 1,

        connection: PlayConnection? = null,
        container: Container? = null,

        durability: Int? = null,

        nbt: MutableJsonObject? = null,
    ): ItemStack {
        val itemStack = ItemStack(item, count)
        if (connection != null || container != null) {
            itemStack.holder = HolderProperty(connection, container)
        }
        if (durability != null) {
            itemStack.durability._durability = durability
        }
        nbt?.let { itemStack.updateNbt(nbt) }

        return itemStack
    }
}
