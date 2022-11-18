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

import de.bixilon.minosoft.data.container.stack.ItemStack
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap

object ContainerUtil {

    fun slotsOf(vararg slots: Pair<Int, ItemStack?>): Int2ObjectMap<ItemStack?> {
        val map = Int2ObjectOpenHashMap<ItemStack?>()
        for ((slot, stack) in slots) {
            val valid = stack == null || stack._valid
            if (!valid) {
                map[slot] = null
                continue
            }
            map[slot] = stack
        }

        return map
    }

    fun section(offset: Int, count: Int): IntRange {
        return offset until offset + count
    }
}
