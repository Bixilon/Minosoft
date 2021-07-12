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
package de.bixilon.minosoft.data.registries.items

import de.bixilon.minosoft.data.Rarities
import de.bixilon.minosoft.data.inventory.ItemStack
import de.bixilon.minosoft.data.player.Hands
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.BlockUsages
import de.bixilon.minosoft.data.registries.inventory.CreativeModeTab
import de.bixilon.minosoft.data.registries.items.armor.ArmorItem
import de.bixilon.minosoft.data.registries.items.armor.HorseArmorItem
import de.bixilon.minosoft.data.registries.items.tools.*
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registries.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.data.registries.registries.registry.Translatable
import de.bixilon.minosoft.gui.rendering.input.camera.hit.RaycastHit
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import de.bixilon.minosoft.util.KUtil.toInt
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.booleanCast

open class Item(
    override val resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : RegistryItem(), Translatable {
    val rarity: Rarities = data["rarity"]?.toInt()?.let { Rarities[it] } ?: Rarities.COMMON
    val maxStackSize: Int = data["max_stack_size"]?.toInt() ?: 64
    val maxDamage: Int = data["max_damage"]?.toInt() ?: 1
    val isFireResistant: Boolean = data["is_fire_resistant"]?.booleanCast() ?: false
    override val translationKey: ResourceLocation? = data["translation_key"]?.asResourceLocation()
    val creativeModeTab: CreativeModeTab? = data["category"]?.toInt()?.let { registries.creativeModeTabRegistry[it] }

    override fun toString(): String {
        return resourceLocation.toString()
    }

    open fun getMiningSpeedMultiplier(connection: PlayConnection, blockState: BlockState, itemStack: ItemStack): Float {
        return 1.0f
    }

    open fun use(connection: PlayConnection, raycastHit: RaycastHit, hands: Hands, itemStack: ItemStack): BlockUsages {
        return BlockUsages.PASS
    }

    companion object : ResourceLocationDeserializer<Item> {
        const val INFINITE_MINING_SPEED_MULTIPLIER = -1.0f

        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: Map<String, Any>): Item {
            check(registries != null) { "Registries is null!" }
            return when (data["class"].unsafeCast<String>()) {
                "BlockItem" -> BlockItem(resourceLocation, registries, data)
                "ArmorItem" -> ArmorItem(resourceLocation, registries, data)
                "SwordItem" -> SwordItem(resourceLocation, registries, data)
                "ToolItem" -> ToolItem(resourceLocation, registries, data)
                "AxeItem" -> AxeItem(resourceLocation, registries, data)
                "BucketItem" -> BucketItem(resourceLocation, registries, data)
                "DyeItem" -> DyeItem(resourceLocation, registries, data)
                "HorseArmorItem" -> HorseArmorItem(resourceLocation, registries, data)
                "SpawnEggItem" -> SpawnEggItem(resourceLocation, registries, data)
                "MusicDiscItem" -> MusicDiscItem(resourceLocation, registries, data)
                "ShovelItem" -> ShovelItem(resourceLocation, registries, data)
                "PickaxeItem" -> PickaxeItem(resourceLocation, registries, data)
                "HoeItem" -> HoeItem(resourceLocation, registries, data)
                //   "Item" -> Item(resourceLocation, data)
                // else -> TODO("Can not find item class: ${data["class"].asString}")
                else -> Item(resourceLocation, registries, data)
            }
        }
    }
}
