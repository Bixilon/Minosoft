/*
 * Minosoft
 * Copyright (C) 2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.gui.input

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.config.key.KeyCodes

enum class MouseButtons {
    LAST,
    LEFT,
    RIGHT,
    MIDDLE,

    BUTTON_1,
    BUTTON_2,
    BUTTON_3,
    BUTTON_4,
    BUTTON_5,
    BUTTON_6,
    BUTTON_7,
    BUTTON_8,
    ;

    companion object : ValuesEnum<MouseButtons> {
        override val VALUES: Array<MouseButtons> = values()
        override val NAME_MAP: Map<String, MouseButtons> = EnumUtil.getEnumValues(VALUES)


        operator fun get(key: KeyCodes): MouseButtons? {
            return when (key) {
                KeyCodes.MOUSE_BUTTON_1 -> BUTTON_1
                KeyCodes.MOUSE_BUTTON_2 -> BUTTON_2
                KeyCodes.MOUSE_BUTTON_3 -> BUTTON_3
                KeyCodes.MOUSE_BUTTON_4 -> BUTTON_4
                KeyCodes.MOUSE_BUTTON_5 -> BUTTON_5
                KeyCodes.MOUSE_BUTTON_6 -> BUTTON_6
                KeyCodes.MOUSE_BUTTON_7 -> BUTTON_7
                KeyCodes.MOUSE_BUTTON_8 -> BUTTON_8
                KeyCodes.MOUSE_BUTTON_LAST -> LAST
                KeyCodes.MOUSE_BUTTON_LEFT -> LEFT
                KeyCodes.MOUSE_BUTTON_RIGHT -> RIGHT
                KeyCodes.MOUSE_BUTTON_MIDDLE -> MIDDLE
                else -> null
            }
        }
    }
}
