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
package de.bixilon.minosoft.data.registries.item.items

import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.Rarities
import de.bixilon.minosoft.data.container.stack.ItemStack
import de.bixilon.minosoft.data.entities.entities.player.Hands
import de.bixilon.minosoft.data.language.translate.Translatable
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.factory.clazz.MultiClassFactory
import de.bixilon.minosoft.data.registries.item.ItemFactories
import de.bixilon.minosoft.data.registries.item.ItemFactory
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registries.registry.codec.ResourceLocationCodec
import de.bixilon.minosoft.gui.rendering.camera.target.targets.BlockTarget
import de.bixilon.minosoft.gui.rendering.camera.target.targets.EntityTarget
import de.bixilon.minosoft.gui.rendering.input.interaction.InteractionResults
import de.bixilon.minosoft.gui.rendering.models.baked.item.BakedItemModel
import de.bixilon.minosoft.gui.rendering.tint.TintProvider
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

open class Item(
    override val resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : RegistryItem(), Translatable {
    val rarity: Rarities = data["rarity"]?.toInt()?.let { Rarities[it] } ?: Rarities.COMMON
    val maxStackSize: Int = data["max_stack_size"]?.toInt() ?: 64
    val maxDurability: Int = data["max_damage"]?.toInt() ?: 1
    val isFireResistant: Boolean = data["is_fire_resistant"]?.toBoolean() ?: false
    override val translationKey: ResourceLocation? = data["translation_key"]?.toResourceLocation()

    open var model: BakedItemModel? = null
    var tintProvider: TintProvider? = null

    override fun toString(): String {
        return resourceLocation.toString()
    }

    open fun getMiningSpeedMultiplier(connection: PlayConnection, blockState: BlockState, stack: ItemStack): Float {
        return 1.0f
    }

    open fun interactBlock(connection: PlayConnection, target: BlockTarget, hand: Hands, stack: ItemStack): InteractionResults {
        return InteractionResults.PASS
    }

    open fun interactEntity(connection: PlayConnection, target: EntityTarget, hand: Hands, stack: ItemStack): InteractionResults {
        return InteractionResults.PASS
    }

    open fun interactEntityAt(connection: PlayConnection, target: EntityTarget, hand: Hands, stack: ItemStack): InteractionResults {
        return InteractionResults.PASS
    }

    open fun interactItem(connection: PlayConnection, hand: Hands, stack: ItemStack): InteractionResults {
        return InteractionResults.PASS
    }

    companion object : ResourceLocationCodec<Item>, ItemFactory<Item>, MultiClassFactory<Item> {
        override val ALIASES = setOf("AirBlockItem")

        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: Map<String, Any>): Item {
            check(registries != null) { "Registries is null!" }

            val className = data["class"]?.toString()
            var factory = ItemFactories[className]
            if (factory == null) {
                Log.log(LogMessageType.VERSION_LOADING, LogLevels.VERBOSE) { "Item for class $className not found, defaulting..." }
                // ToDo: This item class got renamed or is not yet implemented
                factory = if (data["food_properties"] != null) {
                    FoodItem // ToDo: Remove this edge case
                } else {
                    Item
                }
            }
            return factory.build(resourceLocation, registries, data)
        }

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): Item {
            return Item(resourceLocation, registries, data)
        }
    }
}
