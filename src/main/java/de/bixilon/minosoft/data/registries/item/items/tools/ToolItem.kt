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

package de.bixilon.minosoft.data.registries.item.items.tools

import de.bixilon.kutil.primitive.FloatUtil.toFloat
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.item.factory.PixLyzerItemFactory
import de.bixilon.minosoft.data.registries.item.items.PixLyzerItem
import de.bixilon.minosoft.data.registries.registries.Registries


open class ToolItem(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : PixLyzerItem(resourceLocation, registries, data) {
    val durability = data["uses"]?.toInt() ?: 1
    val speed = data["speed"]?.toFloat() ?: 1.0f
    open val attackDamage = data["attack_damage_bonus"]?.toFloat() ?: 1.0f
    val miningLevel = data["level"]?.toInt() ?: 1
    val enchantmentValue = data["enchantment_value"]?.toInt() ?: 1

    companion object : PixLyzerItemFactory<ToolItem> {

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): ToolItem {
            return ToolItem(resourceLocation, registries, data)
        }
    }
}
