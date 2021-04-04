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

import de.bixilon.minosoft.data.mappings.statistics.Statistic;
import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer;
import de.bixilon.minosoft.util.logging.Log;

import java.util.HashMap;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_17W47A;

public class PacketStatistics extends PlayClientboundPacket {
    private final HashMap<Statistic, Integer> statistics = new HashMap<>();

    public PacketStatistics(PlayInByteBuffer buffer) {
        int length = buffer.readVarInt();
        for (int i = 0; i < length; i++) {
            if (buffer.getVersionId() < V_17W47A) { // ToDo
                this.statistics.put(buffer.getConnection().getMapping().getStatisticRegistry().get(buffer.readResourceLocation()), buffer.readVarInt());
            } else {
                var category = buffer.getConnection().getMapping().getStatisticRegistry().get(buffer.readVarInt()); // category before?
                this.statistics.put(buffer.getConnection().getMapping().getStatisticRegistry().get(buffer.readVarInt()), buffer.readVarInt());
            }
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Received player statistics (count=%d)", this.statistics.size()));
    }

    public HashMap<Statistic, Integer> getStatistics() {
        return this.statistics;
    }
}
