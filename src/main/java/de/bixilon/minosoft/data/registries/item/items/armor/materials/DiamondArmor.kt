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
import de.bixilon.minosoft.data.registries.registries.Registries

abstract class DiamondArmor(resourceLocation: ResourceLocation) : ArmorItem(resourceLocation), WearableItem, DefendingItem {


    open class DiamondBoots(resourceLocation: ResourceLocation = this.identifier) : DiamondArmor(resourceLocation), BootsItem {
        override val defense: Int get() = 3

        companion object : ItemFactory<DiamondBoots> {
            override val identifier = minecraft("diamond_boots")

            override fun build(registries: Registries) = DiamondBoots()
        }
    }

    open class DiamondLeggings(resourceLocation: ResourceLocation = this.identifier) : DiamondArmor(resourceLocation), LeggingsItem {
        override val defense: Int get() = 6

        companion object : ItemFactory<DiamondLeggings> {
            override val identifier = minecraft("diamond_leggings")

            override fun build(registries: Registries) = DiamondLeggings()
        }
    }

    open class DiamondChestplate(resourceLocation: ResourceLocation = this.identifier) : DiamondArmor(resourceLocation), ChestplateItem {
        override val defense: Int get() = 8

        companion object : ItemFactory<DiamondChestplate> {
            override val identifier = minecraft("diamond_chestplate")

            override fun build(registries: Registries) = DiamondChestplate()
        }
    }

    open class DiamondHelmet(resourceLocation: ResourceLocation = this.identifier) : DiamondArmor(resourceLocation), HelmetItem {
        override val defense: Int get() = 3

        companion object : ItemFactory<DiamondHelmet> {
            override val identifier = minecraft("diamond_helmet")

            override fun build(registries: Registries) = DiamondHelmet()
        }
    }
}
