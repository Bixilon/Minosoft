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

package de.bixilon.minosoft.data.registries.item.items.tool

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation

enum class ToolLevels(val tag: ResourceLocation?) {
    WOOD(null),
    STONE(minecraft("needs_stone_tool")),
    IRON(minecraft("needs_iron_tool")),
    DIAMOND(minecraft("needs_diamond_tool")),
    ;

    companion object : ValuesEnum<ToolLevels> {
        override val VALUES: Array<ToolLevels> = values()
        val REVERSED: Array<ToolLevels> = VALUES.reversedArray()
        override val NAME_MAP: Map<String, ToolLevels> = EnumUtil.getEnumValues(VALUES)
    }
}
