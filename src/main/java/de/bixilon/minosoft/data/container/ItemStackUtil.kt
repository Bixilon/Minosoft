/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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
import de.bixilon.minosoft.data.container.stack.properties.*
import de.bixilon.minosoft.data.registries.item.items.DurableItem
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.items.legacy.ItemWithMeta
import de.bixilon.minosoft.protocol.network.session.play.PlaySession

object ItemStackUtil {

    fun of(item: Item, count: Int = 1): ItemStack? {
        if (count == 0) return null

        return ItemStack(item, count)
    }

    fun of(item: Item, count: Int, meta: Int, session: PlaySession, nbt: MutableJsonObject): ItemStack? {
        val stack = of(item, count, session, nbt)

        if (stack != null && item is ItemWithMeta) {
            return item.withMeta(stack, meta)
        }

        return stack
    }

    fun of(item: DurableItem, count: Int = 1, durability: Int, session: PlaySession, nbt: MutableJsonObject? = null): ItemStack? {
        item as Item

        val property = when {
            nbt == null || nbt.isEmpty() -> DurabilityProperty(durability = durability)
            else -> DurabilityProperty.of(item, nbt).copy(durability = durability)
        }

        return of(item, count, session, nbt, durability = property)
    }

    fun of(item: Item, count: Int, session: PlaySession, nbt: MutableJsonObject? = null, durability: DurabilityProperty? = null): ItemStack? {
        if (count == 0) return null

        if (nbt == null) return ItemStack(item, count, durability = durability)

        val display = DisplayProperty.of(session.language, nbt)
        val durability = DurabilityProperty.of(item, nbt)
        val enchanting = EnchantingProperty.of(session.registries.enchantment, nbt)
        val hide = HideProperty.of(nbt)

        val nbt = NbtProperty.of(nbt)

        return ItemStack(item, count, display, durability, enchanting, hide, nbt)
    }
}
