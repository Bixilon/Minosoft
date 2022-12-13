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

package de.bixilon.minosoft.data.registries.item.items.food

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.item.factory.ItemFactory
import de.bixilon.minosoft.data.registries.item.items.Item
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.util.KUtil.minecraft

open class AppleItem(resourceLocation: ResourceLocation = this.resourceLocation) : Item(resourceLocation), FoodItem {
    override val nutrition: Int get() = 4

    companion object : ItemFactory<AppleItem> {
        override val resourceLocation = minecraft("apple")

        override fun build(registries: Registries) = AppleItem()
    }

    open class GoldenAppleItem(resourceLocation: ResourceLocation = this.resourceLocation) : AppleItem(resourceLocation) {

        companion object : ItemFactory<GoldenAppleItem> {
            override val resourceLocation = minecraft("golden_apple")

            override fun build(registries: Registries) = GoldenAppleItem()
        }
    }

    open class EnchantedGoldenAppleItem(resourceLocation: ResourceLocation = this.resourceLocation) : GoldenAppleItem(resourceLocation) {

        companion object : ItemFactory<EnchantedGoldenAppleItem> {
            override val resourceLocation = minecraft("enchanted_golden_apple")

            override fun build(registries: Registries) = EnchantedGoldenAppleItem()
        }
    }
}
