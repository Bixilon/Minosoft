package de.bixilon.minosoft.game.datatypes.blocks;

import de.bixilon.minosoft.game.datatypes.Identifier;

public enum Block {
    UNKNOWN(null, -1), // the buggy pink black block
    AIR(new Identifier("air"), 0),
    STONE(new Identifier("stone"), 1),
    GRASS(new Identifier("grass"), 2),
    DIRT(new Identifier("stone"), 3),
    COBBLESTONE(new Identifier("stone"), 4),
    BEDROCK(new Identifier("bedrock"), 7),
    WHITE_WOOL(new Identifier("wool", "white_wool"), 35, 0),
    RED_WOOL(new Identifier("wool", "red_wool"), 35, 14),
    DROPPER_DOWN(new Identifier("dropper"), 158, 0),
    DROPPER_EAST(new Identifier("dropper"), 158, 1),
    DROPPER_NORTH(new Identifier("dropper"), 158, 2),
    DROPPER_SOUTH(new Identifier("dropper"), 158, 3),
    DROPPER_UP(new Identifier("dropper"), 158, 4),
    DROPPER_WEST(new Identifier("dropper"), 158, 5);

    //ToDo all blocks
    //ToDo post water update block states

    Identifier identifier;
    int legacyId;
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

    public static Block getBlockByIdentifier(Identifier identifier) {
        for (Block b : values()) {
            if (b.getIdentifier().equals(identifier)) {
                return b;
            }
        }
        return UNKNOWN;
    }

    public static Block getBlockByLegacy(int id, int data) {
        for (Block b : values()) {
            if (b.getLegacyId() == id && b.getLegacyData() == data) {
                return b;
            }
        }
        return UNKNOWN;
    }

    public static Block getBlockByLegacy(int id) {
        return getBlockByLegacy(id, 0);
    }


}
