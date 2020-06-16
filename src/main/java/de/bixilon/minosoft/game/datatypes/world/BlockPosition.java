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

public class BlockPosition {
    final int x;
    final int y;
    final int z;

    public BlockPosition(int x, short y, int z) {
        // y min -2048, max 2047
        //ToDo check values
        this.x = x;
        this.y = y;
        this.z = z;
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

    @Override
    public boolean equals(Object obj) {
        if (super.equals(obj)) {
            return true;
        }
        BlockPosition pos = (BlockPosition) obj;
        return pos.getX() == getX() && pos.getY() == getY() && pos.getZ() == getZ();
    }

    public ChunkLocation getChunkLocation() {
        return new ChunkLocation(getX() / 16, getZ() / 16);
    }

    @Override
    public String toString() {
        return String.format("%d %d %d", getX(), getY(), getZ());
    }

    @Override
    public int hashCode() {
        return x * y * z;
    }

    public byte getSectionHeight() {
        return (byte) (getY() / 16);
    }

    public ChunkNibbleLocation getNibbleLocation() {
        return new ChunkNibbleLocation(getX() % 16, getY() % 16, getZ() % 16);
    }
}
