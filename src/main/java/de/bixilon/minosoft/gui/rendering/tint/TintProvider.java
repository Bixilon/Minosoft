package de.bixilon.minosoft.gui.rendering.tint;

import de.bixilon.minosoft.data.registries.biomes.Biome;
import de.bixilon.minosoft.data.registries.blocks.BlockState;

import javax.annotation.Nullable;

public interface TintProvider {
    int getColor(@Nullable BlockState blockState, @Nullable Biome biome, int x, int y, int z, int tintIndex);
}
