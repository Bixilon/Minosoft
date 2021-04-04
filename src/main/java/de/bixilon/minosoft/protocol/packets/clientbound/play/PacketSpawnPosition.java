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

import de.bixilon.minosoft.modding.event.events.SpawnPositionChangeEvent;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;
import glm_.vec3.Vec3i;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W03B;

public class PacketSpawnPosition extends PlayClientboundPacket {
    private final Vec3i position;

    public PacketSpawnPosition(PlayInByteBuffer buffer) {
        if (buffer.getVersionId() < V_14W03B) {
            this.position = buffer.readBlockPositionInteger();
            return;
        }
        this.position = buffer.readBlockPosition();
    }

    @Override
    public void handle(PlayConnection connection) {
        connection.fireEvent(new SpawnPositionChangeEvent(connection, this));
        connection.getPlayer().setSpawnPosition(getSpawnPosition());
    }

    @Override
    public void log() {
        Log.protocol("[IN] Received spawn position %s", this.position);
    }

    public Vec3i getSpawnPosition() {
        return this.position;
    }
}
