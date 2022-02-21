/*
 * Minosoft
 * Copyright (C) 2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.inventory.stack.property

import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.kutil.watcher.map.MapDataWatcher.Companion.observeMap
import de.bixilon.kutil.watcher.map.MapDataWatcher.Companion.watchedMap
import de.bixilon.minosoft.data.Rarities
import de.bixilon.minosoft.data.inventory.InventoryDelegate
import de.bixilon.minosoft.data.inventory.stack.ItemStack
import de.bixilon.minosoft.data.registries.enchantment.Enchantment
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.remove

class EnchantingProperty(
    private val stack: ItemStack,
    enchantments: MutableMap<Enchantment, Int> = mutableMapOf(),
    repairCost: Int = 0,
) : Property {
    val enchantments by watchedMap(enchantments) // ToDo: Lock
    var _repairCost = repairCost
    var repairCost by InventoryDelegate(stack, this::_repairCost)

    init {
        this::enchantments.observeMap(this) { stack.holder?.container?.let { it.revision++ } }
    }

    val enchantingRarity: Rarities
        get() {
            val itemRarity = stack.item.item.rarity
            try {
                stack.lock.acquire()
                if (enchantments.isEmpty()) {
                    return itemRarity
                }
            } finally {
                stack.lock.release()
            }

            return when (itemRarity) {
                Rarities.COMMON, Rarities.UNCOMMON -> Rarities.RARE
                Rarities.RARE, Rarities.EPIC -> Rarities.EPIC
            }
        }

    override fun updateNbt(nbt: MutableJsonObject) {
        nbt.remove(REPAIR_COST_TAG)?.toInt()?.let { _repairCost = it }

        nbt.remove(*ENCHANTMENTS_TAG)?.listCast<JsonObject>()?.let {
            val registry = stack.holder?.connection?.registries?.enchantmentRegistry ?: return@let
            for (tag in it) {
                val enchantmentName = tag[ENCHANTMENT_ID_TAG]
                val enchantment = registry[enchantmentName] ?: throw IllegalArgumentException("Unknown enchantment: $enchantmentName")
                val level = tag[ENCHANTMENT_LEVEL_TAG]?.toInt() ?: 1

                enchantments[enchantment] = level
            }
        }
    }

    fun copy(
        stack: ItemStack,
        enchantments: MutableMap<Enchantment, Int> = this.enchantments.toMutableMap(),
        repairCost: Int = this.repairCost,
    ): EnchantingProperty {
        return EnchantingProperty(stack, enchantments, repairCost)
    }

    companion object {
        private const val REPAIR_COST_TAG = "RepairCost"

        private const val ENCHANTMENT_FLATTENING_TAG = "Enchantments"
        private const val ENCHANTMENT_PRE_FLATTENING_TAG = "ench"
        private val ENCHANTMENTS_TAG = arrayOf(ENCHANTMENT_FLATTENING_TAG, ENCHANTMENT_PRE_FLATTENING_TAG)

        private const val ENCHANTMENT_ID_TAG = "id"
        private const val ENCHANTMENT_LEVEL_TAG = "lvl"
    }
}
