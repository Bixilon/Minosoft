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

package de.bixilon.minosoft.data.world.palette;

import de.bixilon.minosoft.config.StaticConfiguration;
import de.bixilon.minosoft.data.mappings.blocks.BlockState;
import de.bixilon.minosoft.data.mappings.versions.VersionMapping;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.logging.Log;

public class IndirectPalette implements Palette {
    private final int bitsPerBlock;
    private int[] palette;
    int versionId;
    VersionMapping mapping;

    public IndirectPalette(int bitsPerBlock) {
        this.bitsPerBlock = bitsPerBlock;
    }

    @Override
    public BlockState blockById(int blockId) {
        if (blockId < this.palette.length) {
            blockId = this.palette[blockId];
        }
        BlockState block = this.mapping.getBlockState(blockId);
        if (StaticConfiguration.DEBUG_MODE) {
            if (block == null) {
                if (blockId == ProtocolDefinition.NULL_BLOCK_ID) {
                    return null;
                }
                String blockName;
                if (this.versionId <= ProtocolDefinition.PRE_FLATTENING_VERSION_ID) {
                    blockName = String.format("%d:%d", blockId >> 4, blockId & 0xF);
                } else {
                    blockName = String.valueOf(blockId);
                }
                Log.warn(String.format("Server sent unknown block: %s", blockName));
                return null;
            }
        }
        return block;
    }

    @Override
    public int getBitsPerBlock() {
        return this.bitsPerBlock;
    }

    @Override
    public void read(PlayInByteBuffer buffer) {
        this.versionId = buffer.getVersionId();
        this.mapping = buffer.getConnection().getMapping();
        this.palette = buffer.readVarIntArray();
    }
}
