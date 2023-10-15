/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.item.items.pixlyzer

import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.Rarities
import de.bixilon.minosoft.data.registries.factory.clazz.MultiClassFactory
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.factory.PixLyzerItemFactories
import de.bixilon.minosoft.data.registries.item.factory.PixLyzerItemFactory
import de.bixilon.minosoft.data.registries.item.items.DurableItem
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.item.stack.StackableItem
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.registries.registries.registry.codec.ResourceLocationCodec
import de.bixilon.minosoft.gui.rendering.tint.TintProvider
import de.bixilon.minosoft.gui.rendering.tint.TintedBlock
import de.bixilon.minosoft.util.KUtil.toResourceLocation

open class PixLyzerItem(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : Item(resourceLocation), DurableItem, StackableItem, TintedBlock {
    override val rarity: Rarities = data["rarity"]?.toInt()?.let { Rarities[it] } ?: Rarities.COMMON
    override val maxStackSize: Int = data["max_stack_size"]?.toInt() ?: 64
    override val maxDurability: Int = data["max_damage"]?.toInt() ?: 1
    val isFireResistant: Boolean = data["is_fire_resistant"]?.toBoolean() ?: false
    override val translationKey: ResourceLocation = data["translation_key"]?.toResourceLocation() ?: super.translationKey
    override var tintProvider: TintProvider? = null


    companion object : ResourceLocationCodec<Item>, PixLyzerItemFactory<Item>, MultiClassFactory<Item> {
        override val ALIASES = setOf("Item", "AirBlockItem")

        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: Map<String, Any>): Item {
            check(registries != null) { "Registries is null!" }

            val className = data["class"]?.toString()
            var factory = PixLyzerItemFactories[className]
            if (factory == null) {
                factory = if (data["food_properties"] != null) {
                    PixLyzerFoodItem // ToDo: Remove this edge case
                } else {
                    PixLyzerItem
                }
            }
            return factory.build(resourceLocation, registries, data)
        }

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): Item {
            return PixLyzerItem(resourceLocation, registries, data)
        }
    }
}
