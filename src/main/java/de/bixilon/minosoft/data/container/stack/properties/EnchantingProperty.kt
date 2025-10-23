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

import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.json.MutableJsonObject
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.enchantment.Enchantment
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.Registry
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.listCast
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.remove

data class EnchantingProperty(
    val enchantments: Map<Enchantment, Int> = emptyMap(),
    val repairCost: Int = 0,
) : Property {

    init {
        assert(repairCost >= 0) { "Repair cost can not be negative" }
    }

    override fun writeNbt(item: Item, version: Version, registries: Registries, nbt: MutableJsonObject) {
        if (repairCost != DEFAULT.repairCost) {
            nbt[REPAIR_COST_TAG] = repairCost
        }

        if (enchantments.isEmpty()) return

        if (!version.flattened) {
            nbt[ENCHANTMENTS_LEGACY_TAG] = this.enchantments.map { mapOf<String, Any>(ENCHANTMENT_ID_TAG to registries.enchantment.getId(it.key), ENCHANTMENT_LEVEL_TAG to it.value.toShort()) }
        } else {
            nbt[ENCHANTMENTS_TAG] = this.enchantments.map { mapOf(ENCHANTMENT_ID_TAG to it.key.identifier.toString(), ENCHANTMENT_LEVEL_TAG to it.value.toShort()) }
        }
    }


    fun with(enchantment: Enchantment, level: Int): EnchantingProperty {
        val current = this.enchantments[enchantment]
        if (current == null && level <= 0) return this
        if (level <= 0) {
            return copy(enchantments = enchantments.toMutableMap().apply { this -= enchantment })
        }

        return copy(enchantments = enchantments.toMutableMap().apply { this[enchantment] = level })
    }

    companion object {
        val DEFAULT = EnchantingProperty()
        private const val REPAIR_COST_TAG = "RepairCost"

        private const val ENCHANTMENTS_TAG = "Enchantments"
        private const val ENCHANTMENTS_LEGACY_TAG = "ench"
        private val ENCHANTMENTS_TAG_LIST = arrayOf(ENCHANTMENTS_TAG, ENCHANTMENTS_LEGACY_TAG)

        private const val ENCHANTMENT_ID_TAG = "id"
        private const val ENCHANTMENT_LEVEL_TAG = "lvl"


        fun of(registry: Registry<Enchantment>, nbt: MutableJsonObject): EnchantingProperty {
            val repairCost = nbt.remove(REPAIR_COST_TAG)?.toInt() ?: 0

            val enchantments = nbt.remove(*ENCHANTMENTS_TAG_LIST)?.listCast<JsonObject>()?.takeIf { it.isNotEmpty() }?.let {
                val enchantments = HashMap<Enchantment, Int>(it.size)
                for (tag in it) {
                    val name = tag[ENCHANTMENT_ID_TAG]
                    val enchantment = registry[name] ?: throw IllegalArgumentException("Unknown enchantment: $name")
                    val level = tag[ENCHANTMENT_LEVEL_TAG]?.toInt() ?: 1
                    if (level <= 0) continue

                    enchantments[enchantment] = level
                }
                return@let enchantments
            }

            if ((enchantments == null || enchantments.isEmpty()) && repairCost == 0) return DEFAULT

            return EnchantingProperty(enchantments ?: emptyMap(), repairCost)
        }
    }
}
