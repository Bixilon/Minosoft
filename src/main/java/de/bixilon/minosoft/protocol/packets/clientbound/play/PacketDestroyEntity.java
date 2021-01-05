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

import de.bixilon.minosoft.modding.event.events.EntityDespawnEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import java.util.Arrays;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W04A;

public class PacketDestroyEntity extends ClientboundPacket {
    int[] entityIds;

    @Override
    public boolean read(InByteBuffer buffer) {
        if (buffer.getVersionId() < V_14W04A) {
            this.entityIds = new int[buffer.readByte()];
        } else {
            this.entityIds = new int[buffer.readVarInt()];
        }

        for (int i = 0; i < this.entityIds.length; i++) {
            this.entityIds[i] = buffer.readEntityId();
        }
        return true;
    }

    @Override
    public void handle(Connection connection) {
        connection.fireEvent(new EntityDespawnEvent(connection, this));

        for (int entityId : getEntityIds()) {
            connection.getPlayer().getWorld().removeEntity(entityId);
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Despawning the following entities: %s", Arrays.toString(this.entityIds)));
    }

    public int[] getEntityIds() {
        return this.entityIds;
    }
}
