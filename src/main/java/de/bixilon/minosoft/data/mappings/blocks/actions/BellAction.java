/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.mappings.blocks.actions;

import com.sun.javafx.scene.traversal.Direction;

public class BellAction implements BlockAction {
    private final Direction direction;

    public BellAction(short unused, short direction) {
        this.direction = Direction.values()[direction];
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public String toString() {
        return String.format("BELL_HIT_%s", direction);
    }
}
