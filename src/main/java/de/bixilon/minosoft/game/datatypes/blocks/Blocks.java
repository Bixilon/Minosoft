package de.bixilon.minosoft.game.datatypes.blocks;

import de.bixilon.minosoft.game.datatypes.BlockPosition;

import java.lang.reflect.InvocationTargetException;

public enum Blocks {
    DIRT(Dirt.class),
    AIR(Air.class);

    private final Class<? extends Block> clazz;

    Blocks(Class<? extends Block> clazz) {
        this.clazz = clazz;
    }

    public static Block getBlockInstance(Class<? extends Block> clazz, BlockPosition pos) {
        try {
            return clazz.getConstructor(BlockPosition.class).newInstance(pos);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException("Invalid block constructor!");
        }
    }

    public static Block getBlockInstance(Blocks b, BlockPosition pos) {
        return getBlockInstance(b.getClazz(), pos);
    }

    public Class<? extends Block> getClazz() {
        return clazz;
    }
}
