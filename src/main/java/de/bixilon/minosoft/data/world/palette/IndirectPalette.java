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

package de.bixilon.minosoft.data.world.palette;

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.data.mappings.CustomMapping;
import de.bixilon.minosoft.data.mappings.blocks.Block;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;

public class IndirectPalette implements Palette {
    final HashBiMap<Integer, Integer> map = HashBiMap.create();
    final byte bitsPerBlock;
    int versionId;
    CustomMapping mapping;

    public IndirectPalette(byte bitsPerBlock) {
        this.bitsPerBlock = bitsPerBlock;
    }

    @Override
    public Block byId(int id) {
        return mapping.getBlockById(map.getOrDefault(id, id));
    }

    @Override
    public byte getBitsPerBlock() {
        return bitsPerBlock;
    }

    @Override
    public void read(InByteBuffer buffer) {
        this.versionId = buffer.getVersionId();
        this.mapping = buffer.getConnection().getMapping();
        int paletteLength = buffer.readVarInt();
        for (int i = 0; i < paletteLength; i++) {
            map.put(i, buffer.readVarInt());
        }
    }
}
