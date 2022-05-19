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

package de.bixilon.minosoft.commands.parser.minecraft.target

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.properties.sort.Sorting
import de.bixilon.minosoft.data.entities.entities.Entity

enum class TargetSelectors(
    val char: Char,
    val sorting: Sorting,
) {
    NEAREST('p', Sorting.NEAREST),
    RANDOM('r', Sorting.RANDOM),
    ALL_PLAYERS('a', Sorting.ARBITRARY),
    ALL_ENTITIES('e', Sorting.ARBITRARY),
    ;


    fun sort(selected: MutableList<Entity>) {
        sorting.sort(selected)
    }

    companion object : ValuesEnum<TargetSelectors> {
        override val VALUES: Array<TargetSelectors> = values()
        override val NAME_MAP: Map<String, TargetSelectors> = EnumUtil.getEnumValues(VALUES)
        val BY_CHAR: MutableMap<Char, TargetSelectors> = mutableMapOf()

        init {
            for (value in VALUES) {
                BY_CHAR[value.char] = value
            }
        }
    }
}
