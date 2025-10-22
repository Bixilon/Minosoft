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

abstract class NetheriteArmor(resourceLocation: ResourceLocation) : ArmorItem(resourceLocation), WearableItem, DefendingArmorItem {


    open class NetheriteBoots(resourceLocation: ResourceLocation = this.identifier) : NetheriteArmor(resourceLocation), BootsItem {
        override val defense: Int get() = 3
        override val maxDurability get() = 481

        companion object : ItemFactory<NetheriteBoots> {
            override val identifier = minecraft("netherite_boots")

            override fun build(registries: Registries, data: JsonObject) = NetheriteBoots()
        }
    }

    open class NetheriteLeggings(resourceLocation: ResourceLocation = this.identifier) : NetheriteArmor(resourceLocation), LeggingsItem {
        override val defense: Int get() = 6
        override val maxDurability get() = 555

        companion object : ItemFactory<NetheriteLeggings> {
            override val identifier = minecraft("netherite_leggings")

            override fun build(registries: Registries, data: JsonObject) = NetheriteLeggings()
        }
    }

    open class NetheriteChestplate(resourceLocation: ResourceLocation = this.identifier) : NetheriteArmor(resourceLocation), ChestplateItem {
        override val defense: Int get() = 8
        override val maxDurability get() = 592

        companion object : ItemFactory<NetheriteChestplate> {
            override val identifier = minecraft("netherite_chestplate")

            override fun build(registries: Registries, data: JsonObject) = NetheriteChestplate()
        }
    }

    open class NetheriteHelmet(resourceLocation: ResourceLocation = this.identifier) : NetheriteArmor(resourceLocation), HelmetItem {
        override val defense: Int get() = 3
        override val maxDurability get() = 275

        companion object : ItemFactory<NetheriteHelmet> {
            override val identifier = minecraft("netherite_helmet")

            override fun build(registries: Registries, data: JsonObject) = NetheriteHelmet()
        }
    }
}
