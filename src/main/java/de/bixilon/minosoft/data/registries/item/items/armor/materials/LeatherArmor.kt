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

package de.bixilon.minosoft.data.registries.item.items.armor.materials

import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.factory.ItemFactory
import de.bixilon.minosoft.data.registries.item.items.armor.ArmorItem
import de.bixilon.minosoft.data.registries.item.items.armor.DefendingItem
import de.bixilon.minosoft.data.registries.item.items.armor.WearableItem
import de.bixilon.minosoft.data.registries.item.items.armor.slots.BootsItem
import de.bixilon.minosoft.data.registries.item.items.armor.slots.ChestplateItem
import de.bixilon.minosoft.data.registries.item.items.armor.slots.HelmetItem
import de.bixilon.minosoft.data.registries.item.items.armor.slots.LeggingsItem
import de.bixilon.minosoft.data.registries.item.items.dye.DyeableItem
import de.bixilon.minosoft.data.registries.registries.Registries

abstract class LeatherArmor(resourceLocation: ResourceLocation) : ArmorItem(resourceLocation), WearableItem, DefendingItem, DyeableItem {


    open class LeatherBoots(resourceLocation: ResourceLocation = this.identifier) : LeatherArmor(resourceLocation), BootsItem {
        override val defense: Int get() = 1

        companion object : ItemFactory<LeatherBoots> {
            override val identifier = minecraft("leather_boots")

            override fun build(registries: Registries) = LeatherBoots()
        }
    }

    open class LeatherLeggings(resourceLocation: ResourceLocation = this.identifier) : LeatherArmor(resourceLocation), LeggingsItem {
        override val defense: Int get() = 2

        companion object : ItemFactory<LeatherLeggings> {
            override val identifier = minecraft("leather_leggings")

            override fun build(registries: Registries) = LeatherLeggings()
        }
    }

    open class LeatherChestplate(resourceLocation: ResourceLocation = this.identifier) : LeatherArmor(resourceLocation), ChestplateItem {
        override val defense: Int get() = 3

        companion object : ItemFactory<LeatherChestplate> {
            override val identifier = minecraft("leather_chestplate")

            override fun build(registries: Registries) = LeatherChestplate()
        }
    }

    open class LeatherHelmet(resourceLocation: ResourceLocation = this.identifier) : LeatherArmor(resourceLocation), HelmetItem {
        override val defense: Int get() = 1

        companion object : ItemFactory<LeatherHelmet> {
            override val identifier = minecraft("leather_helmet")

            override fun build(registries: Registries) = LeatherHelmet()
        }
    }
}
