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

package de.bixilon.minosoft.data.registries.item.items.armor.materials

import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.factory.ItemFactory
import de.bixilon.minosoft.data.registries.item.items.armor.ArmorItem
import de.bixilon.minosoft.data.registries.item.items.armor.DefendingArmorItem
import de.bixilon.minosoft.data.registries.item.items.armor.WearableItem
import de.bixilon.minosoft.data.registries.item.items.armor.slots.BootsItem
import de.bixilon.minosoft.data.registries.item.items.armor.slots.ChestplateItem
import de.bixilon.minosoft.data.registries.item.items.armor.slots.HelmetItem
import de.bixilon.minosoft.data.registries.item.items.armor.slots.LeggingsItem
import de.bixilon.minosoft.data.registries.registries.Registries

abstract class IronArmor(identifier: ResourceLocation) : ArmorItem(identifier), WearableItem, DefendingArmorItem {


    open class IronBoots(identifier: ResourceLocation = this.identifier) : IronArmor(identifier), BootsItem {
        override val defense: Int get() = 2
        override val maxDurability get() = 195

        companion object : ItemFactory<IronBoots> {
            override val identifier = minecraft("iron_boots")

            override fun build(registries: Registries, data: JsonObject) = IronBoots()
        }
    }

    open class IronLeggings(identifier: ResourceLocation = this.identifier) : IronArmor(identifier), LeggingsItem {
        override val defense: Int get() = 5
        override val maxDurability get() = 225

        companion object : ItemFactory<IronLeggings> {
            override val identifier = minecraft("iron_leggings")

            override fun build(registries: Registries, data: JsonObject) = IronLeggings()
        }
    }

    open class IronChestplate(identifier: ResourceLocation = this.identifier) : IronArmor(identifier), ChestplateItem {
        override val defense: Int get() = 6
        override val maxDurability get() = 240

        companion object : ItemFactory<IronChestplate> {
            override val identifier = minecraft("iron_chestplate")

            override fun build(registries: Registries, data: JsonObject) = IronChestplate()
        }
    }

    open class IronHelmet(identifier: ResourceLocation = this.identifier) : IronArmor(identifier), HelmetItem {
        override val defense: Int get() = 2
        override val maxDurability get() = 165

        companion object : ItemFactory<IronHelmet> {
            override val identifier = minecraft("iron_helmet")

            override fun build(registries: Registries, data: JsonObject) = IronHelmet()
        }
    }
}
