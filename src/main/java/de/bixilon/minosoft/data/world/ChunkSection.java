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

package de.bixilon.minosoft.data.world;

import de.bixilon.minosoft.data.entities.block.BlockEntityMetaData;
import de.bixilon.minosoft.data.mappings.blocks.Block;

import java.util.HashMap;

/**
 * Collection of 16x16x16 blocks
 */
public class ChunkSection {
    private final HashMap<InChunkSectionLocation, Block> blocks;
    private final HashMap<InChunkSectionLocation, BlockEntityMetaData> blockEntityMeta = new HashMap<>();

    public ChunkSection(HashMap<InChunkSectionLocation, Block> blocks) {
        this.blocks = blocks;
    }

    public ChunkSection() {
        this.blocks = new HashMap<>();
    }

    public Block getBlock(int x, int y, int z) {
        return getBlock(new InChunkSectionLocation(x, y, z));
    }

    public Block getBlock(InChunkSectionLocation loc) {
        return blocks.get(loc);
    }

    public void setBlock(int x, int y, int z, Block block) {
        setBlock(new InChunkSectionLocation(x, y, z), block);
    }

    public void setBlock(InChunkSectionLocation location, Block block) {
        if (blocks.get(location).equals(block)) {
            return;
        }
        blocks.put(location, block);
        blockEntityMeta.remove(location);
    }

    public void setBlockEntityData(InChunkSectionLocation position, BlockEntityMetaData data) {
        // ToDo check if block is really a block entity (command block, spawner, skull, flower pot)
        blockEntityMeta.put(position, data);
    }

    public BlockEntityMetaData getBlockEntityData(InChunkSectionLocation position) {
        return blockEntityMeta.get(position);
    }

    public void setBlockEntityData(HashMap<InChunkSectionLocation, BlockEntityMetaData> blockEntities) {
        blockEntities.forEach(blockEntityMeta::put);
    }
}
