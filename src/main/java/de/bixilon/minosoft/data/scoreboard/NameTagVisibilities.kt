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

package de.bixilon.minosoft.data.scoreboard

import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.AliasableEnum
import de.bixilon.minosoft.util.enum.ValuesEnum

enum class NameTagVisibilities(vararg names: String) : AliasableEnum {
    ALWAYS,
    HIDE_FOR_ENEMIES("hideForOtherTeams"),
    HIDE_FOR_MATES("hideForOwnTeam"),
    NEVER,
    ;

    override val names: Array<String> = names.toList().toTypedArray()

    companion object : ValuesEnum<NameTagVisibilities> {
        override val VALUES: Array<NameTagVisibilities> = values()
        override val NAME_MAP: Map<String, NameTagVisibilities> = KUtil.getEnumValues(VALUES)

    }
}
