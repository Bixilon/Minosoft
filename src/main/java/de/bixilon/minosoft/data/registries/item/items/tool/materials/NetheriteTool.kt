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
import de.bixilon.minosoft.data.registries.item.items.tool.LeveledTool
import de.bixilon.minosoft.data.registries.item.items.tool.ToolLevels
import de.bixilon.minosoft.data.registries.item.items.tool.axe.AxeItem
import de.bixilon.minosoft.data.registries.item.items.tool.hoe.HoeItem
import de.bixilon.minosoft.data.registries.item.items.tool.pickaxe.PickaxeItem
import de.bixilon.minosoft.data.registries.item.items.tool.shovel.ShovelItem
import de.bixilon.minosoft.data.registries.item.items.tool.sword.SwordItem
import de.bixilon.minosoft.data.registries.registries.Registries

interface NetheriteTool : LeveledTool {
    override val level: ToolLevels get() = ToolLevels.DIAMOND
    override val speed: Float get() = 9.0f
    override val durability: Int get() = 2031

    open class NetheriteSword(identifier: ResourceLocation = this.identifier) : SwordItem(identifier), NetheriteTool {

        companion object : ItemFactory<NetheriteSword> {
            override val identifier = minecraft("netherite_sword")

            override fun build(registries: Registries, data: JsonObject) = NetheriteSword()
        }
    }

    open class NetheriteShovel(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : ShovelItem(identifier, registries, data), NetheriteTool {

        companion object : ItemFactory<NetheriteShovel> {
            override val identifier = minecraft("netherite_shovel")

            override fun build(registries: Registries, data: JsonObject) = NetheriteShovel(registries = registries, data = data)
        }
    }

    open class NetheritePickaxe(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : PickaxeItem(identifier, registries, data), NetheriteTool {

        companion object : ItemFactory<NetheritePickaxe> {
            override val identifier = minecraft("netherite_pickaxe")

            override fun build(registries: Registries, data: JsonObject) = NetheritePickaxe(registries = registries, data = data)
        }
    }

    open class NetheriteAxe(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : AxeItem(identifier, registries, data), NetheriteTool {

        companion object : ItemFactory<NetheriteAxe> {
            override val identifier = minecraft("netherite_axe")

            override fun build(registries: Registries, data: JsonObject) = NetheriteAxe(registries = registries, data = data)
        }
    }

    open class NetheriteHoe(identifier: ResourceLocation = this.identifier, registries: Registries, data: JsonObject) : HoeItem(identifier, registries, data), NetheriteTool {

        companion object : ItemFactory<NetheriteHoe> {
            override val identifier = minecraft("netherite_hoe")

            override fun build(registries: Registries, data: JsonObject) = NetheriteHoe(registries = registries, data = data)
        }
    }
}
