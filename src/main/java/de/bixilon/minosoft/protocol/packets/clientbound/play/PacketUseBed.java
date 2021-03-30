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

import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3i;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W04A;

public class PacketUseBed extends ClientboundPacket {
    private final int entityId;
    private final Vec3i blockPosition;

    public PacketUseBed(InByteBuffer buffer) {
        this.entityId = buffer.readInt();
        if (buffer.getVersionId() < V_14W04A) {
            this.blockPosition = buffer.readBlockPositionByte();
        } else {
            this.blockPosition = buffer.readBlockPosition();
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Entity used bed at %s (entityId=%d)", this.blockPosition, this.entityId));
    }

    public int getEntityId() {
        return this.entityId;
    }

    public Vec3i getBlockPosition() {
        return this.blockPosition;
    }
}
