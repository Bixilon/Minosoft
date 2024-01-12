/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.item.items

import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.registries.item.items.legacy.ItemWithMeta

interface DurableItem : ItemWithMeta {
    val maxDurability: Int

    override fun setMeta(stack: ItemStack, meta: Int) {
        stack.durability.durability = maxDurability - meta // in <1.13 its damage not durability
    }

    override fun getMeta(id: Int, stack: ItemStack): Int {
        val durability = stack._durability ?: return 0
        return maxDurability - durability.durability
    }
}
