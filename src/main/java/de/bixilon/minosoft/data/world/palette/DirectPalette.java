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

import de.bixilon.minosoft.data.mappings.blocks.Block;
import de.bixilon.minosoft.data.mappings.versions.VersionMapping;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_17W47A;
import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_18W10D;

public class DirectPalette implements Palette {
    int versionId;
    VersionMapping mapping;

    @Override
    public Block blockById(int id) {
        return this.mapping.getBlock(id);
    }

    @Override
    public int getBitsPerBlock() {
        if (this.versionId < V_18W10D) {
            return 13;
        }
        return 14;
    }

    @Override
    public void read(InByteBuffer buffer) {
        this.versionId = buffer.getVersionId();
        this.mapping = buffer.getConnection().getMapping();
        if (buffer.getVersionId() < V_17W47A) {
            buffer.readVarInt();
        }
    }
}
