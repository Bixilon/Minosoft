/*
 * Minosoft
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

package de.bixilon.minosoft.data.world;

public record BlockPosition(int x, int y, int z) {
    public ChunkLocation getChunkLocation() {
        return new ChunkLocation(x / 16, z / 16);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getZ() {
        return z;
    }

    public InChunkLocation getInChunkLocation() {
        int x = this.x % 16;
        if (x < 0) {
            x = 16 + x;
        }
        int z = this.z % 16;
        if (z < 0) {
            z = 16 + z;
        }
        return new InChunkLocation(x, this.y, z);
    }

    @Override
    public String toString() {
        return String.format("%d %d %d", x, y, z);
    }
}
