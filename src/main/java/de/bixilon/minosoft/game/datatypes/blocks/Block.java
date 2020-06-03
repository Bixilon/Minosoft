package de.bixilon.minosoft.game.datatypes.blocks;

import de.bixilon.minosoft.game.datatypes.Identifier;

public enum Block {
    AIR(new Identifier("air"), 0),
    DIRT(new Identifier("stone"), 1),
    GRASS(new Identifier("grass"), 2),
    COBBLESTONE(new Identifier("stone"), 4),
    WHITE_WOOL(new Identifier("wool"), 35, 0),
    RED_WOOL(new Identifier("wool", "red_wool"), 35, 14),
    DROPPER(new Identifier("dropper", "dropper"), 158, BlockRotation.RotationType.NORMAL);

    Identifier identifier;
    int legacyId;
    int legacyData;
    BlockRotation.RotationType rotationType;

    Block(Identifier identifier, int legacyId, BlockRotation.RotationType rotationType) {
        this.identifier = identifier;
        this.legacyId = legacyId;
        this.rotationType = rotationType;
    }

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

    public BlockRotation.RotationType getRotationType() {
        return rotationType;
    }
}
