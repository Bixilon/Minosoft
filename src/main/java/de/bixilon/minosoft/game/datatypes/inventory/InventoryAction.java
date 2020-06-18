/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.game.datatypes.inventory;

public enum InventoryAction {
    LEFT_MOUSE_CLICK(0, 0),
    RIGHT_MOUSE_CLICK(0, 1),

    SHIFT_LEFT_MOUSE_CLICK(1, 0),
    SHIFT_RIGHT_MOUSE_CLICK(1, 1),

    HOTKEY_SLOT_1(2, 0),
    HOTKEY_SLOT_2(2, 1),
    HOTKEY_SLOT_3(2, 2),
    HOTKEY_SLOT_4(2, 3),
    HOTKEY_SLOT_5(2, 4),
    HOTKEY_SLOT_6(2, 5),
    HOTKEY_SLOT_7(2, 6),
    HOTKEY_SLOT_8(2, 7),
    HOTKEY_SLOT_9(2, 8),

    MIDDLE_CLICK(3, 0),

    DROP_ITEM(4, 0),
    DROP_STACK(4, 1),
    LEFT_VOID_VOID(4, 0, false),
    RIGHT_VOID_VOID(4, 1, false),

    START_LEFT_MOUSE_DRAG(5, 0, false),
    START_RIGHT_MOUSE_DRAG(5, 4, false),
    ADD_SLOT_LEFT_CLICK(5, 1),
    ADD_SLOT_RIGHT_CLICK(5, 5),
    END_LEFT_MOUSE_DRAG(5, 2, false),
    END_RIGHT_MOUSE_DRAG(5, 6, false),


    DOUBLE_CLICK(6, 0),


    ;
    final byte mode;
    final byte button;
    final boolean hasSlot;

    InventoryAction(byte mode, byte button, boolean hasSlot) {
        this.mode = mode;
        this.button = button;
        this.hasSlot = hasSlot;
    }

    InventoryAction(int mode, int button, boolean hasSlot) {
        this((byte) mode, (byte) button, hasSlot);
    }


    InventoryAction(int mode, int button) {
        this(mode, button, true);
    }

    public byte getButton() {
        return button;
    }

    public byte getMode() {
        return mode;
    }

    public boolean hasSlot() {
        return hasSlot;
    }
}
