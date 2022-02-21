/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.inventory

enum class InventoryActions(
    val mode: Int,
    val button: Int,
    val slot: Boolean,
    val empty: Boolean = false,
) {
    LEFT_MOUSE_CLICK(0, 0, true),
    RIGHT_MOUSE_CLICK(0, 1, true),

    SHIFT_LEFT_MOUSE_CLICK(1, 0, false),
    SHIFT_RIGHT_MOUSE_CLICK(1, 1, false),

    HOTKEY_SLOT_1(2, 0, true),
    HOTKEY_SLOT_2(2, 1, true),
    HOTKEY_SLOT_3(2, 2, true),
    HOTKEY_SLOT_4(2, 3, true),
    HOTKEY_SLOT_5(2, 4, true),
    HOTKEY_SLOT_6(2, 5, true),
    HOTKEY_SLOT_7(2, 6, true),
    HOTKEY_SLOT_8(2, 7, true),
    HOTKEY_SLOT_9(2, 8, true),

    OFFHAND_SWAP(2, 40, true),

    MIDDLE_CLICK(3, 0, true),

    DROP_ITEM(4, 0, true, true),
    DROP_STACK(4, 1, true, true),

    LEFT_VOID_VOID(4, 0, false),
    RIGHT_VOID_VOID(4, 1, false),

    START_LEFT_MOUSE_DRAG(5, 0, false),
    START_RIGHT_MOUSE_DRAG(5, 4, false),
    START_MIDDLE_MOUSE_DRAG(5, 8, false),

    ADD_SLOT_LEFT_CLICK(5, 1, true),
    ADD_SLOT_RIGHT_CLICK(5, 5, true),
    END_LEFT_MIDDLE_CLICK(5, 9, true),

    END_LEFT_MOUSE_DRAG(5, 2, false),
    END_RIGHT_MOUSE_DRAG(5, 6, false),
    END_MIDDLE_MOUSE_DRAG(5, 10, false),

    DOUBLE_CLICK(6, 0, true),
    ;
}
