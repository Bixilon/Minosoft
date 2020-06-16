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

/**
 * Collection of 16x16x16 blocks
 */
public class ChunkNibble {
    private final HashMap<ChunkNibbleLocation, Blocks> blocks;
    private final HashMap<ChunkNibbleLocation, String[]> signs;
    private final HashMap<ChunkNibbleLocation, CompoundTag> blockEntityMeta;

    public ChunkNibble(HashMap<ChunkNibbleLocation, Blocks> blocks) {
        this.blocks = blocks;
        this.signs = new HashMap<>();
        blockEntityMeta = new HashMap<>();
    }

    public ChunkNibble() {
        // empty
        this.blocks = new HashMap<>();
        this.signs = new HashMap<>();
        blockEntityMeta = new HashMap<>();
    }

    public Blocks getBlock(ChunkNibbleLocation loc) {
        return blocks.get(loc);
    }

    public Blocks getBlock(int x, int y, int z) {
        return getBlock(new ChunkNibbleLocation(x, y, z));
    }

    public void setBlock(int x, int y, int z, Blocks block) {
        blocks.put(new ChunkNibbleLocation(x, y, z), block);
    }

    public void setBlock(ChunkNibbleLocation location, Blocks block) {
        blocks.put(location, block);
    }

    public void updateSign(ChunkNibbleLocation location, String[] lines) {
        signs.put(location, lines);
    }

    public String[] getSignText(ChunkNibbleLocation location) {
        return signs.get(location);
    }

    public void setBlockEntityData(ChunkNibbleLocation nibbleLocation, CompoundTag nbt) {
        blockEntityMeta.put(nibbleLocation, nbt);
    }

    public CompoundTag getBlockEntityData(ChunkNibbleLocation nibbleLocation) {
        return blockEntityMeta.get(nibbleLocation);
    }
}
