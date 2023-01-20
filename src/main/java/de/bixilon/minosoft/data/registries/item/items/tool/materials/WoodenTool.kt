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

package de.bixilon.minosoft.data.registries.item.items.tool.materials

import de.bixilon.kutil.json.JsonObject
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.item.factory.ItemFactory
import de.bixilon.minosoft.data.registries.item.items.DurableItem
import de.bixilon.minosoft.data.registries.item.items.tool.ToolLevels
import de.bixilon.minosoft.data.registries.item.items.tool.axe.AxeItem
import de.bixilon.minosoft.data.registries.item.items.tool.hoe.HoeItem
import de.bixilon.minosoft.data.registries.item.items.tool.pickaxe.PickaxeItem
import de.bixilon.minosoft.data.registries.item.items.tool.properties.LeveledTool
import de.bixilon.minosoft.data.registries.item.items.tool.properties.MiningSpeedTool
import de.bixilon.minosoft.data.registries.item.items.tool.shovel.ShovelItem
import de.bixilon.minosoft.data.registries.item.items.tool.sword.SwordItem
import de.bixilon.minosoft.data.registries.registries.Registries

interface WoodenTool : LeveledTool, DurableItem, MiningSpeedTool {
    override val level: ToolLevels get() = ToolLevels.WOOD
    override val miningSpeed: Float get() = 2.0f
    override val maxDurability: Int get() = 59

    open class WoodenSword(identifier: ResourceLocation = this.identifier) : SwordItem(identifier), WoodenTool {

        companion object : ItemFactory<WoodenSword> {
            override val identifier = minecraft("wooden_sword")

            override fun build(registries: Registries, data: JsonObject) = WoodenSword()
        }
    }

    open class WoodenShovel(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : ShovelItem(identifier, registries, data), WoodenTool {

        companion object : ItemFactory<WoodenShovel> {
            override val identifier = minecraft("wooden_shovel")

            override fun build(registries: Registries, data: JsonObject) = WoodenShovel(registries = registries, data = data)
        }
    }

    open class WoodenPickaxe(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : PickaxeItem(identifier, registries, data), WoodenTool {

        companion object : ItemFactory<WoodenPickaxe> {
            override val identifier = minecraft("wooden_pickaxe")

            override fun build(registries: Registries, data: JsonObject) = WoodenPickaxe(registries = registries, data = data)
        }
    }

    open class WoodenAxe(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : AxeItem(identifier, registries, data), WoodenTool {

        companion object : ItemFactory<WoodenAxe> {
            override val identifier = minecraft("wooden_axe")

            override fun build(registries: Registries, data: JsonObject) = WoodenAxe(registries = registries, data = data)
        }
    }

    open class WoodenHoe(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : HoeItem(identifier, registries, data), WoodenTool {

        companion object : ItemFactory<WoodenHoe> {
            override val identifier = minecraft("wooden_hoe")

            override fun build(registries: Registries, data: JsonObject) = WoodenHoe(registries = registries, data = data)
        }
    }
}
