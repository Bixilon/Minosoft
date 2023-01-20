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

interface IronTool : LeveledTool, DurableItem, MiningSpeedTool {
    override val level: ToolLevels get() = ToolLevels.IRON
    override val miningSpeed: Float get() = 6.0f
    override val maxDurability: Int get() = 250

    open class IronSword(identifier: ResourceLocation = this.identifier) : SwordItem(identifier), IronTool {

        companion object : ItemFactory<IronSword> {
            override val identifier = minecraft("iron_sword")

            override fun build(registries: Registries, data: JsonObject) = IronSword()
        }
    }

    open class IronShovel(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : ShovelItem(identifier, registries, data), IronTool {

        companion object : ItemFactory<ShovelItem> {
            override val identifier = minecraft("iron_shovel")

            override fun build(registries: Registries, data: JsonObject) = IronShovel(registries = registries, data = data)
        }
    }

    open class IronPickaxe(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : PickaxeItem(identifier, registries, data), IronTool {

        companion object : ItemFactory<IronPickaxe> {
            override val identifier = minecraft("iron_pickaxe")

            override fun build(registries: Registries, data: JsonObject) = IronPickaxe(registries = registries, data = data)
        }
    }

    open class IronAxe(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : AxeItem(identifier, registries, data), IronTool {

        companion object : ItemFactory<IronAxe> {
            override val identifier = minecraft("iron_axe")

            override fun build(registries: Registries, data: JsonObject) = IronAxe(registries = registries, data = data)
        }
    }

    open class IronHoe(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : HoeItem(identifier, registries, data), IronTool {

        companion object : ItemFactory<IronHoe> {
            override val identifier = minecraft("iron_hoe")

            override fun build(registries: Registries, data: JsonObject) = IronHoe(registries = registries, data = data)
        }
    }
}
