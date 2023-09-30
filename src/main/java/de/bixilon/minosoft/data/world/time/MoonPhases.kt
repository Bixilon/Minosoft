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

package de.bixilon.minosoft.data.world.time

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum

// see https://minecraft.wiki/w/Moon
enum class MoonPhases(val light: Float) {
    FULL_MOON(1.0f),
    WANING_GIBBOUS(0.7f),
    LAST_QUARTER(0.4f),
    WANING_CRESCENT(0.2f),
    NEW_MOON(0.0f),
    WAXING_CRESCENT(0.2f),
    FIRST_QUARTER(0.4f),
    WAXING_GIBBOUS(0.7f),
    ;

    companion object : ValuesEnum<MoonPhases> {
        override val VALUES = values()
        override val NAME_MAP = EnumUtil.getEnumValues(VALUES)
    }
}
