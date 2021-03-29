/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.mappings

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Rarities
import de.bixilon.minosoft.data.mappings.versions.VersionMapping

data class Item(
    override val resourceLocation: ResourceLocation,
    val rarity: Rarities = Rarities.COMMON,
    val maxStackSize: Int = 64,
    val maxDamage: Int = 0,
    val isFireResistant: Boolean = false,
    val descriptionId: String?,
) : RegistryItem {

    companion object : ResourceLocationDeserializer<Item> {
        override fun deserialize(mappings: VersionMapping, resourceLocation: ResourceLocation, data: JsonObject): Item {
            return Item(
                resourceLocation = resourceLocation,
                rarity = data["rarity"]?.asInt?.let { Rarities.VALUES[it] } ?: Rarities.COMMON,
                maxStackSize = data["max_stack_size"]?.asInt ?: 64,
                maxDamage = data["max_damage"]?.asInt ?: 0,
                isFireResistant = data["is_fire_resistant"]?.asBoolean ?: false,
                descriptionId = data["description_id"]?.asString,
            )
        }
    }
}
