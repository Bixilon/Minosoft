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

import de.bixilon.minosoft.game.datatypes.Identifier;

public enum Block {
    UNKNOWN(null, -1), // the buggy pink black block
    AIR(new Identifier("air"), 0),
    STONE(new Identifier("stone"), 1),
    GRASS(new Identifier("grass"), 2),
    DIRT(new Identifier("stone"), 3),
    COBBLESTONE(new Identifier("stone"), 4),
    OAK_WOOD_PLANKS(new Identifier("planks", "oak_planks"), 5),
    SPRUCE_WOOD_PLANKS(new Identifier("planks", "spruce_planks"), 5, 1),
    BIRCH_WOOD_PLANKS(new Identifier("planks", "birch_planks"), 5, 2),
    JUNGLE_WOOD_PLANKS(new Identifier("planks", "jungle_planks"), 5, 3),
    ACACIA_WOOD_PLANKS(new Identifier("planks", "acacia_planks"), 5, 4),
    DARK_OAK_WOOD_PLANKS(new Identifier("planks", "dark_oak_planks"), 5, 5),
    BEDROCK(new Identifier("bedrock"), 7),
    OAK_WOOD(new Identifier("log", "oak_wood"), 17),
    SPRUCE_WOOD(new Identifier("log", "spruce_wood"), 17, 1),
    BIRCH_WOOD(new Identifier("log", "birch_wood"), 17, 2),
    JUNGLE_WOOD(new Identifier("log", "jungle_wood"), 17, 3),
    GLASS(new Identifier("glass"), 20),
    WHITE_WOOL(new Identifier("wool", "white_wool"), 35, 0),
    ORANGE_WOOL(new Identifier("wool", "orange_wool"), 35, 1),
    MAGENTA_WOOL(new Identifier("wool", "magenta_wool"), 35, 2),
    LIGHT_BLUE_WOOL(new Identifier("wool", "light_blue_wool"), 35, 3),
    YELLOW_WOOL(new Identifier("wool", "yellow_wool"), 35, 4),
    LIME_WOOL(new Identifier("wool", "lime_wool"), 35, 5),
    PINK_WOOL(new Identifier("wool", "pink_wool"), 35, 6),
    GRAY_WOOL(new Identifier("wool", "gray_wool"), 35, 7),
    LIGHT_GRAY_WOOL(new Identifier("wool", "light_gray_wool"), 35, 8),
    CYAN_WOOL(new Identifier("wool", "cyan_wool"), 35, 9),
    PURPLE_WOOL(new Identifier("wool", "purple_wool"), 35, 10),
    BLUE_WOOL(new Identifier("wool", "blue_wool"), 35, 11),
    BROWN_WOOL(new Identifier("wool", "brown_wool"), 35, 12),
    GREEN_WOOL(new Identifier("wool", "green_wool"), 35, 13),
    RED_WOOL(new Identifier("wool", "red_wool"), 35, 14),
    BLACK_WOOL(new Identifier("wool", "black_wool"), 35, 15),
    TNT(new Identifier("tnt"), 46),
    DROPPER_DOWN(new Identifier("dropper"), 158, 0),
    DROPPER_EAST(new Identifier("dropper"), 158, 1),
    DROPPER_NORTH(new Identifier("dropper"), 158, 2),
    DROPPER_SOUTH(new Identifier("dropper"), 158, 3),
    DROPPER_UP(new Identifier("dropper"), 158, 4),
    DROPPER_WEST(new Identifier("dropper"), 158, 5);

    //ToDo all blocks
    //ToDo post water update block states

    final Identifier identifier;
    final int legacyId;
    int legacyData;

    Block(Identifier identifier, int legacyId, int legacyData) {
        this.identifier = identifier;
        this.legacyId = legacyId;
        this.legacyData = legacyData;
    }

    Block(Identifier identifier, int legacyId) {
        this.identifier = identifier;
        this.legacyId = legacyId;
    }

    public Identifier getIdentifier() {
        return identifier;
    }


    public int getLegacyId() {
        return legacyId;
    }

    public int getLegacyData() {
        return legacyData;
    }

    public static Block byIdentifier(Identifier identifier) {
        for (Block b : values()) {
            if (b.getIdentifier().equals(identifier)) {
                return b;
            }
        }
        return UNKNOWN;
    }

    public static Block byLegacy(int id, int data) {
        for (Block b : values()) {
            if (b.getLegacyId() == id && b.getLegacyData() == data) {
                return b;
            }
        }
        return UNKNOWN;
    }

    public static Block byLegacy(int id) {
        return byLegacy(id, 0);
    }


}
