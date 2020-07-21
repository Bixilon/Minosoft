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

package de.bixilon.minosoft.game.datatypes.blocks.actions;

import de.bixilon.minosoft.game.datatypes.Direction;

public class PistonAction implements BlockAction {
    final PistonStates status;
    final Direction direction;

    public PistonAction(byte status, byte direction) {
        this.status = PistonStates.byId(status);
        this.direction = Direction.byId(direction);
    }

    @Override
    public String toString() {
        return String.format("PISTON_%s:%s", status, direction);
    }

    public enum PistonStates {
        PUSH(0),
        PULL(1);

        final byte id;

        PistonStates(int id) {
            this.id = (byte) id;
        }

        public static PistonStates byId(int id) {
            for (PistonStates s : values()) {
                if (s.getId() == id) {
                    return s;
                }
            }
            return null;
        }

        public byte getId() {
            return id;
        }
    }
}
