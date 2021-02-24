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

package de.bixilon.minosoft.data.mappings.blocks;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public enum BlockRotations {
    NONE,
    // mostly sign, but general
    SOUTH(0),
    SOUTH_SOUTH_WEST(1),
    SOUTH_WEST(2),
    WEST_SOUTH_WEST(3),
    WEST(4),
    WEST_NORTH_WEST(5),
    NORTH_WEST(6),
    NORTH_NORTH_WEST(7),
    NORTH(8),
    NORTH_NORTH_EAST(9),
    NORTH_EAST(10),
    EAST_NORTH_EAST(11),
    EAST(12),
    EAST_SOUTH_EAST(13),
    SOUTH_EAST(14),
    SOUTH_SOUTH_EAST(15),

    // stairs?
    NORTH_SOUTH,
    EAST_WEST,
    ASCENDING_EAST,
    ASCENDING_WEST,
    ASCENDING_NORTH,
    ASCENDING_SOUTH,

    UP,
    DOWN,

    DOWN_EAST,
    DOWN_WEST,
    DOWN_NORTH,
    DOWN_SOUTH,
    UP_EAST,
    EAST_UP,
    UP_WEST,
    WEST_UP,
    UP_NORTH,
    NORTH_UP,
    UP_SOUTH,
    SOUTH_UP,

    // log, portal
    AXIS_X("x"),
    AXIS_Y("y"),
    AXIS_Z("z");

    public static final HashMap<Object, BlockRotations> ROTATION_MAPPING = new HashMap<>();

    static {
        // add all to hashmap
        for (BlockRotations rotation : values()) {
            ROTATION_MAPPING.put(rotation.name().toLowerCase(), rotation);
            rotation.getAliases().forEach((alias) -> ROTATION_MAPPING.put(alias, rotation));
        }
    }

    private final HashSet<Object> aliases;

    BlockRotations() {
        this.aliases = new HashSet<>();
    }

    BlockRotations(Object... alias) {
        this.aliases = new HashSet<>(Arrays.asList(alias));
    }

    public HashSet<Object> getAliases() {
        return this.aliases;
    }
}
