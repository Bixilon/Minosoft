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

import de.bixilon.minosoft.data.MapSet;
import de.bixilon.minosoft.data.VersionValueMap;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;

import static de.bixilon.minosoft.protocol.protocol.Versions.*;

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
        buffer.writeEntityId(this.entityId);
        if (buffer.getVersionId() < V_14W04A) {
            buffer.writeByte((byte) this.action.getId(buffer.getVersionId()));
            buffer.writeInt(this.parameter);
        } else {
            buffer.writeVarInt(this.action.getId(buffer.getVersionId()));
            buffer.writeVarInt(this.parameter);
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Sending entity action packet (entityId=%d, action=%s, parameter=%d)", this.entityId, this.action, this.parameter));
    }

    public enum EntityActions {
        SNEAK(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 0)}),
        UN_SNEAK(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 1)}),
        LEAVE_BED(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 2)}),
        START_SPRINTING(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 3)}),
        STOP_SPRINTING(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 4)}),
        START_HORSE_JUMP(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 5)}),
        STOP_HORSE_JUMP(new MapSet[]{new MapSet<>(V_15W41A, 6)}), // ToDo: when did they change? really in 77?
        OPEN_HORSE_INVENTORY(new MapSet[]{new MapSet<>(LOWEST_VERSION_SUPPORTED, 6), new MapSet<>(V_15W41A, 7)}),
        START_ELYTRA_FLYING(new MapSet[]{new MapSet<>(V_15W41A, 8)});

        final VersionValueMap<Integer> valueMap;

        EntityActions(MapSet<Integer, Integer>[] values) {
            this.valueMap = new VersionValueMap<>(values);
        }

        public static EntityActions byId(int id, int versionId) {
            for (EntityActions action : values()) {
                if (action.getId(versionId) == id) {
                    return action;
                }
            }
            return null;
        }

        public int getId(int versionId) {
            Integer ret = this.valueMap.get(versionId);
            if (ret == null) {
                return -2;
            }
            return ret;
        }
    }
}
