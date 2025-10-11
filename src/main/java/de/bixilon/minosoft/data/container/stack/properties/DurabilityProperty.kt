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

package de.bixilon.minosoft.data.container.stack.properties

import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.item.items.DurableItem
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.registries.Registries

data class DurabilityProperty(
    val durability: Int,
    val unbreakable: Boolean = false,
) : Property {

    init {
        assert(durability > 0) { "Invalid durability: $durability" }
    }

    override fun writeNbt(registries: Registries, nbt: MutableJsonObject) {
        // TODO: durability
        if (unbreakable) {
            nbt[UNBREAKABLE_TAG] = 1.toByte()
        }
    }

    companion object {
        private const val UNBREAKABLE_TAG = "unbreakable"
        private const val DAMAGE_TAG = "Damage"

        fun of(item: Item, nbt: MutableJsonObject): DurabilityProperty {
            item as DurableItem

            val durability = nbt.remove(DAMAGE_TAG)?.toInt()?.let { item.maxDurability - it } ?: item.maxDurability
            val unbreakable = nbt.remove(UNBREAKABLE_TAG)?.toBoolean() ?: false

            return DurabilityProperty(durability, unbreakable)
        }
    }
}
