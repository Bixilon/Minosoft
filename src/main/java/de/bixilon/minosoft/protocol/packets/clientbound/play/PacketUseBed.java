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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W04A;

public class PacketUseBed extends ClientboundPacket {
    int entityId;
    BlockPosition position;

    @Override
    public boolean read(InByteBuffer buffer) {
        this.entityId = buffer.readInt();
        if (buffer.getVersionId() < V_14W04A) {
            this.position = buffer.readBlockPositionByte();
        } else {
            this.position = buffer.readPosition();
        }
        return true;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Entity used bed at %s (entityId=%d)", this.position, this.entityId));
    }

    public int getEntityId() {
        return this.entityId;
    }

    public BlockPosition getPosition() {
        return this.position;
    }
}
