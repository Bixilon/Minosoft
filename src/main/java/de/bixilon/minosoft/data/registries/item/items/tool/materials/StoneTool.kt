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

interface StoneTool : LeveledTool, DurableItem, MiningSpeedTool {
    override val level: ToolLevels get() = ToolLevels.STONE
    override val miningSpeed: Float get() = 4.0f
    override val maxDurability: Int get() = 131

    open class StoneSword(identifier: ResourceLocation = this.identifier) : SwordItem(identifier), StoneTool {

        companion object : ItemFactory<StoneSword> {
            override val identifier = minecraft("stone_sword")

            override fun build(registries: Registries, data: JsonObject) = StoneSword()
        }
    }

    open class StoneShovel(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : ShovelItem(identifier, registries, data), StoneTool {

        companion object : ItemFactory<StoneShovel> {
            override val identifier = minecraft("stone_shovel")

            override fun build(registries: Registries, data: JsonObject) = StoneShovel(registries = registries, data = data)
        }
    }

    open class StonePickaxe(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : PickaxeItem(identifier, registries, data), StoneTool {

        companion object : ItemFactory<StonePickaxe> {
            override val identifier = minecraft("stone_pickaxe")

            override fun build(registries: Registries, data: JsonObject) = StonePickaxe(registries = registries, data = data)
        }
    }

    open class StoneAxe(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : AxeItem(identifier, registries, data), StoneTool {

        companion object : ItemFactory<StoneAxe> {
            override val identifier = minecraft("stone_axe")

            override fun build(registries: Registries, data: JsonObject) = StoneAxe(registries = registries, data = data)
        }
    }

    open class StoneHoe(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : HoeItem(identifier, registries, data), StoneTool {

        companion object : ItemFactory<StoneHoe> {
            override val identifier = minecraft("stone_hoe")

            override fun build(registries: Registries, data: JsonObject) = StoneHoe(registries = registries, data = data)
        }
    }
}
