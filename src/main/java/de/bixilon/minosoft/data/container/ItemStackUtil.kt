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

package de.bixilon.minosoft.data.container

import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.container.stack.property.HolderProperty
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.legacy.ItemWithMeta
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

object ItemStackUtil {

    fun of(item: Item, connection: PlayConnection, count: Int, meta: Int, nbt: MutableMap<String, Any>): ItemStack {
        if (connection.version.flattened) {
            return of(item, count, connection, durability = meta, nbt = nbt)
        }
        val stack = of(item, count, connection, nbt = nbt)

        if (item is ItemWithMeta) {
            item.setMeta(stack, meta)
        }

        return stack
    }

    fun of(
        item: Item,
        count: Int = 1,

        connection: PlayConnection? = null,
        container: Container? = null,

        durability: Int? = null,

        nbt: MutableJsonObject? = null,
    ): ItemStack {
        val stack = ItemStack(item, count)
        if (connection != null || container != null) {
            stack.holder = HolderProperty(connection, container)
        }
        if (durability != null) {
            stack.durability._durability = durability
        }
        nbt?.let { stack.updateNbt(nbt) }

        return stack
    }
}
