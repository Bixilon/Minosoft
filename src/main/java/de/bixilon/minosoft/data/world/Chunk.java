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
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;

import java.util.HashMap;

/**
 * Collection of 16 chunks sections
 */
public class Chunk {
    private final HashMap<Integer, ChunkSection> sections;

    public Chunk(HashMap<Integer, ChunkSection> sections) {
        this.sections = sections;
    }

    public Block getBlock(InChunkLocation location) {
        return getBlock(location.getX(), location.getY(), location.getZ());
    }

    public Block getBlock(int x, int y, int z) {
        int section = (y / ProtocolDefinition.SECTION_HEIGHT_Y);
        if (!this.sections.containsKey(section)) {
            return null;
        }
        return this.sections.get(section).getBlock(x, y % 16, z);
    }

    public void setBlock(int x, int y, int z, Block block) {
        int section = y / ProtocolDefinition.SECTION_HEIGHT_Y;
        createSection(section);
        this.sections.get(section).setBlock(x, y % 16, z, block);
    }

    void createSection(int height) {
        if (this.sections.get(height) == null) {
            // section was empty before, creating it
            this.sections.put(height, new ChunkSection());
        }
    }

    public void setBlocks(HashMap<InChunkLocation, Block> blocks) {
        blocks.forEach(this::setBlock);
    }

    public void setBlock(InChunkLocation location, Block block) {
        int section = (location.getY() / ProtocolDefinition.SECTION_HEIGHT_Y);
        createSection(section);
        this.sections.get(section).setBlock(location.getInChunkSectionLocation(), block);
    }

    public void setBlockEntityData(InChunkLocation position, BlockEntityMetaData data) {
        ChunkSection section = this.sections.get((position.getY() / ProtocolDefinition.SECTION_HEIGHT_Y));
        if (section == null) {
            return;
        }
        section.setBlockEntityData(position.getInChunkSectionLocation(), data);
    }

    public BlockEntityMetaData getBlockEntityData(InChunkLocation position) {
        ChunkSection section = this.sections.get((position.getY() / ProtocolDefinition.SECTION_HEIGHT_Y));
        if (section == null) {
            return null;
        }
        return section.getBlockEntityData(position.getInChunkSectionLocation());
    }

    public void setBlockEntityData(HashMap<InChunkLocation, BlockEntityMetaData> blockEntities) {
        blockEntities.forEach(this::setBlockEntityData);
    }

    public HashMap<Integer, ChunkSection> getSections() {
        return this.sections;
    }

    public ChunkSection getSectionOrCreate(int sectionHeight) {
        ChunkSection section = this.sections.get(sectionHeight);
        if (section == null) {
            section = new ChunkSection();
            this.sections.put(sectionHeight, section);
        }
        return section;
    }
}
