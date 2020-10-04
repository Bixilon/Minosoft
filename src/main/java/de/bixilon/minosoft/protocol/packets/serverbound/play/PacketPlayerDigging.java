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

import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;

public class PacketPlayerDigging implements ServerboundPacket {
    final DiggingStatus status;
    final BlockPosition position;
    final DiggingFaces face;

    public PacketPlayerDigging(DiggingStatus status, BlockPosition position, DiggingFaces face) {
        this.status = status;
        this.position = position;
        this.face = face;
    }

    @Override
    public OutPacketBuffer write(Connection connection) {
        OutPacketBuffer buffer = new OutPacketBuffer(connection, Packets.Serverbound.PLAY_PLAYER_DIGGING);
        if (buffer.getProtocolId() < 49) { //ToDo
            buffer.writeByte((byte) status.ordinal());
        } else {
            buffer.writeVarInt(status.ordinal());
        }

        if (buffer.getProtocolId() < 7) {
            if (position == null) {
                buffer.writeInt(0);
                buffer.writeByte((byte) 0);
                buffer.writeInt(0);
            } else {
                buffer.writeBlockPositionByte(position);
            }
        } else {
            buffer.writePosition(position);
        }

        buffer.writeByte(face.getId());
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Send player digging packet (status=%s, position=%s, face=%s)", status, position, face));
    }

    public enum DiggingStatus {
        START_DIGGING,
        CANCELLED_DIGGING,
        FINISHED_DIGGING,
        DROP_ITEM_STACK,
        DROP_ITEM,
        SHOOT_ARROW__FINISH_EATING,
        SWAP_ITEMS_IN_HAND;

        public static DiggingStatus byId(int id) {
            return values()[id];
        }
    }

    public enum DiggingFaces {
        BOTTOM(0),
        TOP(1),
        NORTH(2),
        SOUTH(3),
        WEST(4),
        EAST(5),
        SPECIAL(255);

        final byte id;

        DiggingFaces(int id) {
            this.id = (byte) id;
        }

        public byte getId() {
            return id;
        }
    }
}
