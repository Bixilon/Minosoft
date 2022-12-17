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

package de.bixilon.minosoft.data.registries.item.items.armor.materials

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.item.factory.ItemFactory
import de.bixilon.minosoft.data.registries.item.items.armor.ArmorItem
import de.bixilon.minosoft.data.registries.item.items.armor.DefendingItem
import de.bixilon.minosoft.data.registries.item.items.armor.WearableItem
import de.bixilon.minosoft.data.registries.item.items.armor.slots.BootsItem
import de.bixilon.minosoft.data.registries.item.items.armor.slots.ChestplateItem
import de.bixilon.minosoft.data.registries.item.items.armor.slots.HelmetItem
import de.bixilon.minosoft.data.registries.item.items.armor.slots.LeggingsItem
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.util.KUtil.minecraft

abstract class ChainmailArmor(resourceLocation: ResourceLocation) : ArmorItem(resourceLocation), WearableItem, DefendingItem {


    open class ChainmailBoots(resourceLocation: ResourceLocation = this.resourceLocation) : ChainmailArmor(resourceLocation), BootsItem {
        override val defense: Int get() = 1

        companion object : ItemFactory<ChainmailBoots> {
            override val resourceLocation = minecraft("chainmail_boots")

            override fun build(registries: Registries) = ChainmailBoots()
        }
    }

    open class ChainmailLeggings(resourceLocation: ResourceLocation = this.resourceLocation) : ChainmailArmor(resourceLocation), LeggingsItem {
        override val defense: Int get() = 4

        companion object : ItemFactory<ChainmailLeggings> {
            override val resourceLocation = minecraft("chainmail_leggings")

            override fun build(registries: Registries) = ChainmailLeggings()
        }
    }

    open class ChainmailChestplate(resourceLocation: ResourceLocation = this.resourceLocation) : ChainmailArmor(resourceLocation), ChestplateItem {
        override val defense: Int get() = 5

        companion object : ItemFactory<ChainmailChestplate> {
            override val resourceLocation = minecraft("chainmail_chestplate")

            override fun build(registries: Registries) = ChainmailChestplate()
        }
    }

    open class ChainmailHelmet(resourceLocation: ResourceLocation = this.resourceLocation) : ChainmailArmor(resourceLocation), HelmetItem {
        override val defense: Int get() = 2

        companion object : ItemFactory<ChainmailHelmet> {
            override val resourceLocation = minecraft("chainmail_helmet")

            override fun build(registries: Registries) = ChainmailHelmet()
        }
    }
}
