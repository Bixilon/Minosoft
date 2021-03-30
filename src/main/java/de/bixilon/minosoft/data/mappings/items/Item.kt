/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.mappings.items

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Rarities
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.inventory.CreativeModeTab
import de.bixilon.minosoft.data.mappings.items.armor.ArmorItem
import de.bixilon.minosoft.data.mappings.registry.RegistryItem
import de.bixilon.minosoft.data.mappings.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.data.mappings.versions.VersionMapping

open class Item(
    override val resourceLocation: ResourceLocation,
    data: JsonObject,
    versionMapping: VersionMapping,
) : RegistryItem {
    val rarity: Rarities = data["rarity"]?.asInt?.let { Rarities.VALUES[it] } ?: Rarities.COMMON
    val maxStackSize: Int = data["max_stack_size"]?.asInt ?: 64
    val maxDamage: Int = data["max_damage"]?.asInt ?: 64
    val isFireResistant: Boolean = data["is_fire_resistant"]?.asBoolean ?: false
    val translationKey: String? = data["description_id"]?.asString
    val creativeModeTab: CreativeModeTab? = data["category"]?.asInt?.let { versionMapping.creativeModeTabRegistry.get(it) }

    override fun toString(): String {
        return resourceLocation.toString()
    }

    companion object : ResourceLocationDeserializer<Item> {
        override fun deserialize(mappings: VersionMapping, resourceLocation: ResourceLocation, data: JsonObject): Item {
            return when (data["class"].asString) {
                "ArmorItem" -> ArmorItem(resourceLocation, data, mappings)
                //   "Item" -> Item(resourceLocation, data)
                // else -> TODO("Can not find item class: ${data["class"].asString}")
                else -> Item(resourceLocation, data, mappings)
            }
        }
    }
}
