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

interface DiamondTool : LeveledTool, DurableItem, MiningSpeedTool {
    override val level: ToolLevels get() = ToolLevels.DIAMOND
    override val miningSpeed: Float get() = 8.0f
    override val maxDurability: Int get() = 1561

    open class DiamondSword(identifier: ResourceLocation = this.identifier) : SwordItem(identifier), DiamondTool {

        companion object : ItemFactory<DiamondSword> {
            override val identifier = minecraft("diamond_sword")

            override fun build(registries: Registries, data: JsonObject) = DiamondSword()
        }
    }

    open class DiamondShovel(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : ShovelItem(identifier, registries, data), DiamondTool {

        companion object : ItemFactory<DiamondShovel> {
            override val identifier = minecraft("diamond_shovel")

            override fun build(registries: Registries, data: JsonObject) = DiamondShovel(registries = registries, data = data)
        }
    }

    open class DiamondPickaxe(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : PickaxeItem(identifier, registries, data), DiamondTool {

        companion object : ItemFactory<DiamondPickaxe> {
            override val identifier = minecraft("diamond_pickaxe")

            override fun build(registries: Registries, data: JsonObject) = DiamondPickaxe(registries = registries, data = data)
        }
    }

    open class DiamondAxe(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : AxeItem(identifier, registries, data), DiamondTool {

        companion object : ItemFactory<DiamondAxe> {
            override val identifier = minecraft("diamond_axe")

            override fun build(registries: Registries, data: JsonObject) = DiamondAxe(registries = registries, data = data)
        }
    }

    open class DiamondHoe(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : HoeItem(identifier, registries, data), DiamondTool {

        companion object : ItemFactory<DiamondHoe> {
            override val identifier = minecraft("diamond_hoe")

            override fun build(registries: Registries, data: JsonObject) = DiamondHoe(registries = registries, data = data)
        }
    }
}
