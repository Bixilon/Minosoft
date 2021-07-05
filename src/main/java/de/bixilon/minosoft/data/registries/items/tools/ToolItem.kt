/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.items.tools

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.items.Item
import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.util.KUtil.toInt
import de.bixilon.minosoft.util.KUtil.unsafeCast


open class ToolItem(
    resourceLocation: ResourceLocation,
    registries: Registries,
    data: Map<String, Any>,
) : Item(resourceLocation, registries, data) {
    val durability = data["uses"]?.toInt() ?: 1
    val speed = data["speed"]?.unsafeCast<Float>() ?: 1.0f
    open val attackDamage = data["attack_damage_bonus"]?.unsafeCast<Float>() ?: 1.0f
    val miningLevel = data["level"]?.toInt() ?: 1
    val enchantmentValue = data["enchantment_value"]?.toInt() ?: 1
}
