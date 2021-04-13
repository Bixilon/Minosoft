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

package de.bixilon.minosoft.data.mappings.blocks.actions;

import de.bixilon.minosoft.data.Directions;

public class PistonAction implements BlockAction {
    private final PistonStates status;
    private final Directions direction;

    public PistonAction(int status, int direction) {
        this.status = PistonStates.byId(status);
        this.direction = Directions.byId(direction);
    }

    @Override
    public String toString() {
        return String.format("PISTON_%s:%s", this.status, this.direction);
    }

    public enum PistonStates {
        PUSH,
        PULL;

        private static final PistonStates[] PISTON_STATES = values();

        public static PistonStates byId(int id) {
            return PISTON_STATES[id];
        }
    }
}
