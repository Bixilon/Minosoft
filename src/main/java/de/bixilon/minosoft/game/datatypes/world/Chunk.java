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

import de.bixilon.minosoft.game.datatypes.blocks.Block;

import java.util.HashMap;
import java.util.Map;

/**
 * Collection of 16 chunks nibbles
 */
public class Chunk {
    private final HashMap<Byte, ChunkNibble> nibbles;

    public Chunk(HashMap<Byte, ChunkNibble> chunks) {
        this.nibbles = chunks;
    }

    public Block getBlock(int x, int y, int z) {
        if (x > 16 || y > 255 || z > 16 || x < 0 || y < 0 || z < 0) {
            throw new IllegalArgumentException(String.format("Invalid chunk location %s %s %s", x, y, z));
        }
        byte section = (byte) (y / 16);
        return nibbles.get(section).getBlock(x, y % 16, z);
    }

    public void setBlock(int x, int y, int z, Block block) {
        byte section = (byte) (y / 16);
        createSection(section);
        nibbles.get(section).setBlock(x, y % 16, z, block);
    }

    public void setBlock(InChunkLocation location, Block block) {
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

    public void setBlocks(HashMap<InChunkLocation, Block> blocks) {
        for (Map.Entry<InChunkLocation, Block> set : blocks.entrySet()) {
            setBlock(set.getKey(), set.getValue());
        }
    }
}
