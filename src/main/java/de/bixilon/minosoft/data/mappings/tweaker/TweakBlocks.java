package de.bixilon.minosoft.data.mappings.tweaker;

import de.bixilon.minosoft.data.mappings.ModIdentifier;
import de.bixilon.minosoft.data.mappings.blocks.Block;
import de.bixilon.minosoft.data.mappings.blocks.BlockProperties;

public final class TweakBlocks {
    public static final Block GRASS_BLOCK_SNOWY_YES = new Block(new ModIdentifier("grass"), BlockProperties.GRASS_SNOWY_YES);
    public static final Block GRASS_BLOCK_SNOWY_NO = new Block(new ModIdentifier("grass"), BlockProperties.GRASS_SNOWY_NO);
    public static final Block SNOW = new Block(new ModIdentifier("snow"));
    public static final Block SNOW_LAYER = new Block(new ModIdentifier("snow_layer"));
}
