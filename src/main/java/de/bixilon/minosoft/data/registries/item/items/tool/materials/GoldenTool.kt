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

interface GoldenTool : LeveledTool, DurableItem, MiningSpeedTool {
    override val level: ToolLevels get() = ToolLevels.WOOD
    override val miningSpeed: Float get() = 12.0f
    override val maxDurability: Int get() = 32

    open class GoldenSword(identifier: ResourceLocation = this.identifier) : SwordItem(identifier), GoldenTool {

        companion object : ItemFactory<GoldenSword> {
            override val identifier = minecraft("golden_sword")

            override fun build(registries: Registries, data: JsonObject) = GoldenSword()
        }
    }

    open class GoldenShovel(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : ShovelItem(identifier, registries, data), GoldenTool {

        companion object : ItemFactory<GoldenShovel> {
            override val identifier = minecraft("golden_shovel")

            override fun build(registries: Registries, data: JsonObject) = GoldenShovel(registries = registries, data = data)
        }
    }

    open class GoldenPickaxe(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : PickaxeItem(identifier, registries, data), GoldenTool {

        companion object : ItemFactory<GoldenPickaxe> {
            override val identifier = minecraft("golden_pickaxe")

            override fun build(registries: Registries, data: JsonObject) = GoldenPickaxe(registries = registries, data = data)
        }
    }

    open class GoldenAxe(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : AxeItem(identifier, registries, data), GoldenTool {

        companion object : ItemFactory<GoldenAxe> {
            override val identifier = minecraft("golden_axe")

            override fun build(registries: Registries, data: JsonObject) = GoldenAxe(registries = registries, data = data)
        }
    }

    open class GoldenHoe(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : HoeItem(identifier, registries, data), GoldenTool {

        companion object : ItemFactory<GoldenHoe> {
            override val identifier = minecraft("golden_hoe")

            override fun build(registries: Registries, data: JsonObject) = GoldenHoe(registries = registries, data = data)
        }
    }
}
