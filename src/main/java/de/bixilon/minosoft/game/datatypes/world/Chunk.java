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

package de.bixilon.minosoft.game.datatypes.world;

import de.bixilon.minosoft.game.datatypes.blocks.Blocks;
import de.bixilon.minosoft.nbt.tag.CompoundTag;

import java.util.HashMap;
import java.util.Map;

/**
 * Collection of 16 chunks nibbles
 */
public class Chunk {
    private final HashMap<Byte, ChunkNibble> nibbles;
    private final HashMap<InChunkLocation, String[]> signs;
    private final HashMap<InChunkLocation, CompoundTag> blockEntityMeta;

    public Chunk(HashMap<Byte, ChunkNibble> chunks) {
        this.nibbles = chunks;
        signs = new HashMap<>();
        blockEntityMeta = new HashMap<>();
    }

    public Blocks getBlock(int x, int y, int z) {
        if (x > 15 || y > 255 || z > 15 || x < 0 || y < 0 || z < 0) {
            throw new IllegalArgumentException(String.format("Invalid chunk location %s %s %s", x, y, z));
        }
        byte section = (byte) (y / 16);
        return nibbles.get(section).getBlock(x, y % 16, z);
    }

    public Blocks getBlock(InChunkLocation location) {
        return getBlock(location.getX(), location.getY(), location.getZ());
    }

    public void setBlock(int x, int y, int z, Blocks block) {
        byte section = (byte) (y / 16);
        createSection(section);
        nibbles.get(section).setBlock(x, y % 16, z, block);
    }

    public void setBlock(InChunkLocation location, Blocks block) {
        byte section = (byte) (location.getY() / 16);
        createSection(section);
        nibbles.get(section).setBlock(location.getChunkNibbleLocation(), block);
    }

    private void createSection(byte section) {
        if (nibbles.get(section) == null) {
            // nibble was empty before, creating it
            nibbles.put(section, new ChunkNibble());
        }
    }

    public void setBlocks(HashMap<InChunkLocation, Blocks> blocks) {
        for (Map.Entry<InChunkLocation, Blocks> set : blocks.entrySet()) {
            setBlock(set.getKey(), set.getValue());
        }
    }

    public void updateSign(BlockPosition position, String[] lines) {
        signs.put(position.getInChunkLocation(), lines);
    }

    public String[] getSignText(BlockPosition position) {
        return signs.get(position.getInChunkLocation());
    }

    public void setBlockEntityData(BlockPosition position, CompoundTag nbt) {
        blockEntityMeta.put(position.getInChunkLocation(), nbt);
    }

    public CompoundTag getBlockEntityData(BlockPosition position) {
        return blockEntityMeta.get(position.getInChunkLocation());
    }
}
