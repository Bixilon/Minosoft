/*
 * Codename Minosoft
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

package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.game.datatypes.MapSet;
import de.bixilon.minosoft.game.datatypes.VersionValueMap;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;

public class PacketEntityAction implements ServerboundPacket {
    final int entityId;
    final EntityActions action;
    final int parameter; // only for horse (jump boost)

    public PacketEntityAction(int entityId, EntityActions action) {
        this.entityId = entityId;
        this.action = action;
        this.parameter = 0;
    }

    public PacketEntityAction(int entityId, EntityActions action, int parameter) {
        this.entityId = entityId;
        this.action = action;
        this.parameter = parameter;
    }

    @Override
    public OutPacketBuffer write(Connection connection) {
        OutPacketBuffer buffer = new OutPacketBuffer(connection, Packets.Serverbound.PLAY_ENTITY_ACTION);
        if (buffer.getProtocolId() < 7) {
            buffer.writeInt(entityId);
            buffer.writeByte((byte) action.getId(buffer.getProtocolId()));
            buffer.writeInt(parameter);
        } else {
            buffer.writeVarInt(entityId);
            buffer.writeVarInt(action.getId(buffer.getProtocolId()));
            buffer.writeVarInt(parameter);
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Sending entity action packet (entityId=%d, action=%s, parameter=%d)", entityId, action, parameter));
    }

    public enum EntityActions {
        SNEAK(new MapSet[]{new MapSet<>(0, 0)}),
        UN_SNEAK(new MapSet[]{new MapSet<>(0, 1)}),
        LEAVE_BED(new MapSet[]{new MapSet<>(0, 2)}),
        START_SPRINTING(new MapSet[]{new MapSet<>(0, 3)}),
        STOP_SPRINTING(new MapSet[]{new MapSet<>(0, 4)}),
        START_HORSE_JUMP(new MapSet[]{new MapSet<>(0, 5)}),
        STOP_HORSE_JUMP(new MapSet[]{new MapSet<>(77, 6)}), // ToDo: when did they change? really in 77?
        OPEN_HORSE_INVENTORY(new MapSet[]{new MapSet<>(0, 6), new MapSet<>(77, 7)}),
        START_ELYTRA_FLYING(new MapSet[]{new MapSet<>(77, 8)});

        final VersionValueMap<Integer> valueMap;

        EntityActions(MapSet<Integer, Integer>[] values) {
            valueMap = new VersionValueMap<>(values, true);
        }

        public static EntityActions byId(int id, int protocolId) {
            for (EntityActions action : values()) {
                if (action.getId(protocolId) == id) {
                    return action;
                }
            }
            return null;
        }

        public int getId(int protocolId) {
            Integer ret = valueMap.get(protocolId);
            if (ret == null) {
                return -2;
            }
            return ret;
        }
    }
}
