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

package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.protocol.packets.serverbound.PlayServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPlayByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W04A;

public class ClientActionServerboundPacket implements PlayServerboundPacket {
    private final ClientStates status;

    public ClientActionServerboundPacket(ClientStates status) {
        this.status = status;
    }

    @Override
    public void write(OutPlayByteBuffer buffer) {
        if (buffer.getVersionId() < V_14W04A) {
            buffer.writeByte((byte) this.status.ordinal());
        } else {
            buffer.writeVarInt(this.status.ordinal());
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending client status packet (status=%s)", this.status));
    }

    public enum ClientStates {
        PERFORM_RESPAWN,
        REQUEST_STATISTICS,
        OPEN_INVENTORY;

        private static final ClientStates[] CLIENT_STATES = values();

        public static ClientStates byId(int id) {
            return CLIENT_STATES[id];
        }
    }
}
