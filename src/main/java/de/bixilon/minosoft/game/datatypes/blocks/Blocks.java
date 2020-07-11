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

package de.bixilon.minosoft.game.datatypes.blocks;

import de.bixilon.minosoft.game.datatypes.Color;

public enum Blocks {
    UNKNOWN(-1), // the buggy pink black block (any other block)
    AIR(0),
    STONE(1),
    GRASS(2),
    DIRT(3),
    COBBLESTONE(4),
    OAK_WOOD_PLANKS(5),
    SPRUCE_WOOD_PLANKS(OAK_WOOD_PLANKS, 1),
    BIRCH_WOOD_PLANKS(OAK_WOOD_PLANKS, 2),
    JUNGLE_WOOD_PLANKS(OAK_WOOD_PLANKS, 3),
    ACACIA_WOOD_PLANKS(OAK_WOOD_PLANKS, 4),
    DARK_OAK_WOOD_PLANKS(OAK_WOOD_PLANKS, 5),
    BEDROCK(7),
    OAK_WOOD(17),
    SPRUCE_WOOD(OAK_WOOD, 1),
    BIRCH_WOOD(OAK_WOOD, 2),
    JUNGLE_WOOD(OAK_WOOD, 3),
    GLASS(20),
    WHITE_WOOL(35, Color.WHITE),
    ORANGE_WOOL(WHITE_WOOL, Color.ORANGE),
    MAGENTA_WOOL(WHITE_WOOL, Color.MAGENTA),
    LIGHT_BLUE_WOOL(WHITE_WOOL, Color.LIGHT_BLUE),
    YELLOW_WOOL(WHITE_WOOL, Color.YELLOW),
    LIME_WOOL(WHITE_WOOL, Color.LIME),
    PINK_WOOL(WHITE_WOOL, Color.PINK),
    GRAY_WOOL(WHITE_WOOL, Color.GRAY),
    LIGHT_GRAY_WOOL(WHITE_WOOL, Color.SILVER),
    CYAN_WOOL(WHITE_WOOL, Color.CYAN),
    PURPLE_WOOL(WHITE_WOOL, Color.PURPLE),
    BLUE_WOOL(WHITE_WOOL, Color.BLUE),
    BROWN_WOOL(WHITE_WOOL, Color.BROWN),
    GREEN_WOOL(WHITE_WOOL, Color.GREEN),
    RED_WOOL(WHITE_WOOL, Color.RED),
    BLACK_WOOL(WHITE_WOOL, Color.BLACK),
    TNT(46),
    STANDING_SIGN_SOUTH(63, 0),
    STANDING_SIGN_SOUTH_SOUTH_WEST(STANDING_SIGN_SOUTH, 1),
    STANDING_SIGN_SOUTH_WEST(STANDING_SIGN_SOUTH, 2),
    STANDING_SIGN_WEST_SOUTH_WEST(STANDING_SIGN_SOUTH, 3),
    STANDING_SIGN_WEST(STANDING_SIGN_SOUTH, 4),
    STANDING_SIGN_WEST_NORTH_WEST(STANDING_SIGN_SOUTH, 5),
    STANDING_SIGN_NORTH_WEST(STANDING_SIGN_SOUTH, 6),
    STANDING_SIGN_NORTH_NORTH_WEST(STANDING_SIGN_SOUTH, 7),
    STANDING_SIGN_NORTH(STANDING_SIGN_SOUTH, 8),
    STANDING_SIGN_NORTH_NORTH_EAST(STANDING_SIGN_SOUTH, 9),
    STANDING_SIGN_NORTH_EAST(STANDING_SIGN_SOUTH, 10),
    STANDING_SIGN_EAST_NORTH_EAST(STANDING_SIGN_SOUTH, 11),
    STANDING_SIGN_EAST(STANDING_SIGN_SOUTH, 12),
    STANDING_SIGN_EAST_SOUTH_EAST(STANDING_SIGN_SOUTH, 13),
    STANDING_SIGN_SOUTH_EAST(STANDING_SIGN_SOUTH, 14),
    STANDING_SIGN_SOUTH_SOUTH_EAST(STANDING_SIGN_SOUTH, 15),
    WALL_SIGN_EAST(68, 0),
    WALL_SIGN_NORTH(WALL_SIGN_EAST, 1),
    WALL_SIGN_SOUTH(WALL_SIGN_EAST, 2),
    WALL_SIGN_WEST(WALL_SIGN_EAST, 3),
    DROPPER_DOWN(158, 0),
    DROPPER_EAST(DROPPER_DOWN, 1),
    DROPPER_NORTH(DROPPER_DOWN, 2),
    DROPPER_SOUTH(DROPPER_DOWN, 3),
    DROPPER_UP(DROPPER_DOWN, 4),
    DROPPER_WEST(DROPPER_DOWN, 5);

    // ToDo all blocks
    // ToDo post water update block states

    final int id;
    final int data;

    Blocks(int id, int data) {
        this.id = id;
        this.data = data;
    }

    Blocks(int id, Color color) {
        // used for wool, etc
        this.id = id;
        this.data = color.getId();
    }

    Blocks(Blocks block, Color color) {
        // used for wool, etc
        this.id = block.getId();
        this.data = color.getId();
    }

    Blocks(Blocks block, int data) {
        // used for existing blocks with different data values
        this.id = block.getId();
        this.data = data;
    }

    Blocks(int id) {
        this.id = id;
        this.data = 0;
    }

    public static Blocks byId(int id, int data) {
        for (Blocks b : values()) {
            if (b.getId() == id && b.getData() == data) {
                return b;
            }
        }
        return UNKNOWN;
    }

    public static Blocks byId(int id) {
        return byId(id, 0);
    }


    public int getId() {
        return id;
    }

    public int getData() {
        return data;
    }
}
