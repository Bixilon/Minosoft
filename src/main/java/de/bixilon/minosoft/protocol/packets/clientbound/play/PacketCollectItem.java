/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.modding.event.events.CollectItemAnimationEvent;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_14W04A;
import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_16W32A;

public class PacketCollectItem extends PlayClientboundPacket {
    private final int itemEntityId;
    private final int collectorEntityId;
    private int count;

    public PacketCollectItem(PlayInByteBuffer buffer) {
        this.itemEntityId = buffer.readEntityId();
        if (buffer.getVersionId() < V_14W04A) {
            this.collectorEntityId = buffer.readInt();
            return;
        }
        this.collectorEntityId = buffer.readVarInt();
        if (buffer.getVersionId() >= V_16W32A) {
            this.count = buffer.readVarInt();
        }
    }

    @Override
    public void handle(PlayConnection connection) {
        if (connection.fireEvent(new CollectItemAnimationEvent(connection, this))) {
            return;
        }
        // ToDo
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Item %d was collected by %d (count=%s)", this.itemEntityId, this.collectorEntityId, ((this.count == 0) ? "?" : this.count)));
    }

    public int getItemEntityId() {
        return this.itemEntityId;
    }

    public int getCollectorEntityId() {
        return this.collectorEntityId;
    }

    public int getCount() {
        return this.count;
    }
}
