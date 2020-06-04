package de.bixilon.minosoft.game.datatypes.blocks;

import de.bixilon.minosoft.game.datatypes.Identifier;

public enum Block {
    AIR(new Identifier("air"), 0),
    DIRT(new Identifier("stone"), 1),
    GRASS(new Identifier("grass"), 2),
    COBBLESTONE(new Identifier("stone"), 4),
    WHITE_WOOL(new Identifier("wool"), 35, 0),
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

}
