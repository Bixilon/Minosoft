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

package de.bixilon.minosoft.data.world.map

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import kotlin.collections.Map

enum class MapPinTypes {
    WHITE_ARROW,
    GREEN_ARROW,
    RED_ARROW,
    BLUE_ARROW,
    WHITE_CROSS,
    RED_POINTER,
    WHITE_CIRCLE,
    BLUE_SQUARE,
    SMALL_WHITE_CIRCLE,
    MANSION,
    TEMPLE,
    WHITE_BANNER,
    ORANGE_BANNER,
    MAGENTA_BANNER,
    LIGHT_BLUE_BANNER,
    YELLOW_BANNER,
    LIME_BANNER,
    PINK_BANNER,
    GRAY_BANNER,
    LIGHT_GRAY_BANNER,
    CYAN_BANNER,
    PURPLE_BANNER,
    BLUE_BANNER,
    BROWN_BANNER,
    GREEN_BANNER,
    RED_BANNER,
    BLACK_BANNER,
    TREASURE_MARKER,
    ;

    companion object : ValuesEnum<MapPinTypes> {
        override val VALUES: Array<MapPinTypes> = values()
        override val NAME_MAP: Map<String, MapPinTypes> = EnumUtil.getEnumValues(VALUES)
    }
}
