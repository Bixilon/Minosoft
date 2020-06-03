package de.bixilon.minosoft.game.datatypes.blocks;

import de.bixilon.minosoft.game.datatypes.BlockPosition;

public class Dirt implements Block {
    final BlockPosition position;

    public Dirt(BlockPosition position) {
        this.position = position;
    }

    @Override
    public BlockPosition getBlockPosition() {
        return position;
    }

    @Override
    public int getLegacyId() {
        return 0;
    }

    @Override
    public String getLegacyIdentifier() {
        return "minecraft:dirt";
    }
}
