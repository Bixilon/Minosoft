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
        switch (version) {
            case VERSION_1_7_10:
                buffer.writeInt(entityId);
                buffer.writeByte((byte) action.getId(version));
                buffer.writeInt(parameter);
                break;
            default:
                buffer.writeVarInt(entityId);
                buffer.writeVarInt(action.getId(version));
                buffer.writeVarInt(parameter);
                break;
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Sending entity action packet (entityId=%d, action=%s, parameter=%d)", entityId, action, parameter));
    }

    public enum EntityActions {
        SNEAK(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 0)}),
        UN_SNEAK(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 1)}),
        LEAVE_BED(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 2)}),
        START_SPRINTING(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 3)}),
        STOP_SPRINTING(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 4)}),
        START_HORSE_JUMP(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 5)}),
        STOP_HORSE_JUMP(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_9_4, 6)}),
        OPEN_HORSE_INVENTORY(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_7_10, 6), new MapSet<>(ProtocolVersion.VERSION_1_9_4, 7)}),
        START_ELYTRA_FLYING(new MapSet[]{new MapSet<>(ProtocolVersion.VERSION_1_9_4, 8)});

        final VersionValueMap<Integer> valueMap;

        EntityActions(MapSet<ProtocolVersion, Integer>[] values) {
            valueMap = new VersionValueMap<>(values, true);
        }

        public static EntityActions byId(int id, int protocolId) {
            for (EntityActions action : values()) {
                if (action.getId(version) == id) {
                    return action;
                }
            }
            return null;
        }

        public int getId(ProtocolVersion version) {
            Integer ret = valueMap.get(version);
            if (ret == null) {
                return -2;
            }
            return ret;
        }
    }
}
