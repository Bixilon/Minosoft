/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.mappings.items

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.Rarities
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.mappings.blocks.BlockUsages
import de.bixilon.minosoft.data.mappings.inventory.CreativeModeTab
import de.bixilon.minosoft.data.mappings.items.armor.ArmorItem
import de.bixilon.minosoft.data.mappings.items.armor.HorseArmorItem
import de.bixilon.minosoft.data.mappings.items.tools.*
import de.bixilon.minosoft.data.mappings.registry.RegistryItem
import de.bixilon.minosoft.data.mappings.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.data.mappings.registry.Translatable
import de.bixilon.minosoft.data.mappings.versions.Registries
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.gui.rendering.input.camera.RaycastHit
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3i

open class Item(
    override val resourceLocation: ResourceLocation,
    registries: Registries,
    data: JsonObject,
) : RegistryItem, Translatable {
    val rarity: Rarities = data["rarity"]?.asInt?.let { Rarities[it] } ?: Rarities.COMMON
    val maxStackSize: Int = data["max_stack_size"]?.asInt ?: 64
    val maxDamage: Int = data["max_damage"]?.asInt ?: 1
    val isFireResistant: Boolean = data["is_fire_resistant"]?.asBoolean ?: false
    override val translationKey: String? = data["translation_key"]?.asString
    val creativeModeTab: CreativeModeTab? = data["category"]?.asInt?.let { registries.creativeModeTabRegistry[it] }

    override fun toString(): String {
        return resourceLocation.toString()
    }

    open fun use(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, raycastHit: RaycastHit, hands: Hands, itemStack: ItemStack): BlockUsages {
        return BlockUsages.PASS
    }

    companion object : ResourceLocationDeserializer<Item> {
        override fun deserialize(mappings: Registries?, resourceLocation: ResourceLocation, data: JsonObject): Item {
            check(mappings != null) { "Registries is null!" }
            return when (data["class"].asString) {
                "BlockItem" -> BlockItem(resourceLocation, mappings, data)
                "ArmorItem" -> ArmorItem(resourceLocation, mappings, data)
                "SwordItem" -> SwordItem(resourceLocation, mappings, data)
                "ToolItem" -> ToolItem(resourceLocation, mappings, data)
                "MiningToolItem" -> MiningToolItem(resourceLocation, mappings, data)
                "AxeItem" -> AxeItem(resourceLocation, mappings, data)
                "BucketItem" -> BucketItem(resourceLocation, mappings, data)
                "DyeItem" -> DyeItem(resourceLocation, mappings, data)
                "HorseArmorItem" -> HorseArmorItem(resourceLocation, mappings, data)
                "SpawnEggItem" -> SpawnEggItem(resourceLocation, mappings, data)
                "MusicDiscItem" -> MusicDiscItem(resourceLocation, mappings, data)
                "ShovelItem" -> ShovelItem(resourceLocation, mappings, data)
                "HoeItem" -> HoeItem(resourceLocation, mappings, data)
                //   "Item" -> Item(resourceLocation, data)
                // else -> TODO("Can not find item class: ${data["class"].asString}")
                else -> Item(resourceLocation, mappings, data)
            }
        }
    }
}
