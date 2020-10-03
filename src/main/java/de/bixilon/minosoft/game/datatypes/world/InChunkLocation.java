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

/**
 * Chunk X, Y and Z location (max 16x16x16)
 */
public class InChunkLocation {
    final int x;
    final int y;
    final int z;

    public InChunkLocation(int x, int y, int z) {
        // x 0 - 16
        // y 0 - 255
        // z 0 - 16
        if (x > 15 || y > 255 || z > 15 || x < 0 || y < 0 || z < 0) {
            throw new IllegalArgumentException(String.format("Invalid chunk location %s %s %s", x, y, z));
        }
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        InChunkLocation that = (InChunkLocation) obj;
        return getX() == that.getX() && getY() == that.getY() && getZ() == that.getZ();
    }

    public ChunkNibbleLocation getChunkNibbleLocation() {
        return new ChunkNibbleLocation(getX(), getY() % 16, getZ());
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return String.format("%d %d %d", getX(), getY(), getZ());
    }

    public int getZ() {
        return z;
    }
}
