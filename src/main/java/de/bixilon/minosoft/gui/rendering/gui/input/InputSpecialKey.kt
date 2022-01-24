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

enum class InputSpecialKey {
    KEY_ESCAPE,
    KEY_ENTER,
    KEY_TAB,
    KEY_BACKSPACE,
    KEY_INSERT,
    KEY_DELETE,
    KEY_RIGHT,
    KEY_LEFT,
    KEY_DOWN,
    KEY_UP,
    KEY_PAGE_UP,
    KEY_PAGE_DOWN,
    KEY_HOME,
    KEY_END,
    KEY_CAPS_LOCK,
    KEY_SCROLL_LOCK,
    KEY_NUM_LOCK,
    KEY_PRINT_SCREEN,
    KEY_PAUSE,
    KEY_F1,
    KEY_F2,
    KEY_F3,
    KEY_F4,
    KEY_F5,
    KEY_F6,
    KEY_F7,
    KEY_F8,
    KEY_F9,
    KEY_F10,
    KEY_F11,
    KEY_F12,
    KEY_F13,
    KEY_F14,
    KEY_F15,
    KEY_F16,
    KEY_F17,
    KEY_F18,
    KEY_F19,
    KEY_F20,
    KEY_F21,
    KEY_F22,
    KEY_F23,
    KEY_F24,
    KEY_F25,
    KEY_KP_0,
    KEY_KP_1,
    KEY_KP_2,
    KEY_KP_3,
    KEY_KP_4,
    KEY_KP_5,
    KEY_KP_6,
    KEY_KP_7,
    KEY_KP_8,
    KEY_KP_9,


    KEY_LEFT_SHIFT,
    KEY_LEFT_CONTROL,
    KEY_LEFT_ALT,
    KEY_LEFT_SUPER,
    KEY_RIGHT_SHIFT,
    KEY_RIGHT_CONTROL,
    KEY_RIGHT_ALT,
    KEY_RIGHT_SUPER,
    KEY_MENU,
    KEY_LAST,

    ;

    companion object : ValuesEnum<InputSpecialKey> {
        override val VALUES: Array<InputSpecialKey> = values()
        override val NAME_MAP: Map<String, InputSpecialKey> = EnumUtil.getEnumValues(VALUES)

        operator fun get(keyCode: KeyCodes): InputSpecialKey? {
            return when (keyCode) {
                KeyCodes.KEY_ESCAPE -> KEY_ESCAPE
                KeyCodes.KEY_ENTER -> KEY_ENTER
                KeyCodes.KEY_TAB -> KEY_TAB
                KeyCodes.KEY_BACKSPACE -> KEY_BACKSPACE
                KeyCodes.KEY_INSERT -> KEY_INSERT
                KeyCodes.KEY_DELETE -> KEY_DELETE
                KeyCodes.KEY_RIGHT -> KEY_RIGHT
                KeyCodes.KEY_LEFT -> KEY_LEFT
                KeyCodes.KEY_DOWN -> KEY_DOWN
                KeyCodes.KEY_UP -> KEY_UP
                KeyCodes.KEY_PAGE_UP -> KEY_PAGE_UP
                KeyCodes.KEY_PAGE_DOWN -> KEY_PAGE_DOWN
                KeyCodes.KEY_HOME -> KEY_HOME
                KeyCodes.KEY_END -> KEY_END
                KeyCodes.KEY_CAPS_LOCK -> KEY_CAPS_LOCK
                KeyCodes.KEY_SCROLL_LOCK -> KEY_SCROLL_LOCK
                KeyCodes.KEY_NUM_LOCK -> KEY_NUM_LOCK
                KeyCodes.KEY_PRINT_SCREEN -> KEY_PRINT_SCREEN
                KeyCodes.KEY_PAUSE -> KEY_PAUSE
                KeyCodes.KEY_F1 -> KEY_F1
                KeyCodes.KEY_F2 -> KEY_F2
                KeyCodes.KEY_F3 -> KEY_F3
                KeyCodes.KEY_F4 -> KEY_F4
                KeyCodes.KEY_F5 -> KEY_F5
                KeyCodes.KEY_F6 -> KEY_F6
                KeyCodes.KEY_F7 -> KEY_F7
                KeyCodes.KEY_F8 -> KEY_F8
                KeyCodes.KEY_F9 -> KEY_F9
                KeyCodes.KEY_F10 -> KEY_F10
                KeyCodes.KEY_F11 -> KEY_F11
                KeyCodes.KEY_F12 -> KEY_F12
                KeyCodes.KEY_F13 -> KEY_F13
                KeyCodes.KEY_F14 -> KEY_F14
                KeyCodes.KEY_F15 -> KEY_F15
                KeyCodes.KEY_F16 -> KEY_F16
                KeyCodes.KEY_F17 -> KEY_F17
                KeyCodes.KEY_F18 -> KEY_F18
                KeyCodes.KEY_F19 -> KEY_F19
                KeyCodes.KEY_F20 -> KEY_F20
                KeyCodes.KEY_F21 -> KEY_F21
                KeyCodes.KEY_F22 -> KEY_F22
                KeyCodes.KEY_F23 -> KEY_F23
                KeyCodes.KEY_F24 -> KEY_F24
                KeyCodes.KEY_F25 -> KEY_F25
                KeyCodes.KEY_KP_0 -> KEY_KP_0
                KeyCodes.KEY_KP_1 -> KEY_KP_1
                KeyCodes.KEY_KP_2 -> KEY_KP_2
                KeyCodes.KEY_KP_3 -> KEY_KP_3
                KeyCodes.KEY_KP_4 -> KEY_KP_4
                KeyCodes.KEY_KP_5 -> KEY_KP_5
                KeyCodes.KEY_KP_6 -> KEY_KP_6
                KeyCodes.KEY_KP_7 -> KEY_KP_7
                KeyCodes.KEY_KP_8 -> KEY_KP_8
                KeyCodes.KEY_KP_9 -> KEY_KP_9


                KeyCodes.KEY_LEFT_SHIFT -> KEY_LEFT_SHIFT
                KeyCodes.KEY_LEFT_CONTROL -> KEY_LEFT_CONTROL
                KeyCodes.KEY_LEFT_ALT -> KEY_LEFT_ALT
                KeyCodes.KEY_LEFT_SUPER -> KEY_LEFT_SUPER
                KeyCodes.KEY_RIGHT_SHIFT -> KEY_RIGHT_SHIFT
                KeyCodes.KEY_RIGHT_CONTROL -> KEY_RIGHT_CONTROL
                KeyCodes.KEY_RIGHT_ALT -> KEY_RIGHT_ALT
                KeyCodes.KEY_RIGHT_SUPER -> KEY_RIGHT_SUPER
                KeyCodes.KEY_MENU -> KEY_MENU
                KeyCodes.KEY_LAST -> KEY_LAST

                else -> null
            }
        }

    }
}
