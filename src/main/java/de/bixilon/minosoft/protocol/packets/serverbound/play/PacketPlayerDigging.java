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
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketPlayerDigging implements ServerboundPacket {
    final DiggingStatus status;
    final BlockPosition position;
    final DiggingFace face;


    public PacketPlayerDigging(DiggingStatus status, BlockPosition position, DiggingFace face) {
        this.status = status;
        this.position = position;
        this.face = face;
    }


    @Override
    public OutPacketBuffer write(ProtocolVersion version) {
        OutPacketBuffer buffer = new OutPacketBuffer(version, version.getPacketCommand(Packets.Serverbound.PLAY_PLAYER_DIGGING));
        switch (version) {
            case VERSION_1_7_10:
                buffer.writeByte((byte) status.getId());
                if (position == null) {
                    buffer.writeInt(0);
                    buffer.writeByte((byte) 0);
                    buffer.writeInt(0);
                } else {
                    buffer.writeBlockPositionByte(position);
                }
                buffer.writeByte(face.getId());
                break;
            case VERSION_1_8:
                buffer.writeByte((byte) status.getId());
                if (position == null) {
                    buffer.writeLong(0L);
                } else {
                    buffer.writePosition(position);
                }
                buffer.writeByte(face.getId());
                break;
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
            case VERSION_1_13_2:
            case VERSION_1_14_4:
                buffer.writeVarInt(status.getId());
                if (position == null) {
                    buffer.writeLong(0L);
                } else {
                    buffer.writePosition(position);
                }
                buffer.writeByte(face.getId());
                break;
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Send player digging packet (status=%s, position=%s, face=%s)", status, position.toString(), face));
    }

    public enum DiggingStatus {
        START_DIGGING(0),
        CANCELLED_DIGGING(1),
        FINISHED_DIGGING(2),
        DROP_ITEM_STACK(3),
        DROP_ITEM(4),
        SHOOT_ARROW__FINISH_EATING(5),
        SWAP_ITEMS_IN_HAND(6);

        final int id;

        DiggingStatus(int id) {
            this.id = id;
        }

        public int getId() {
            return id;
        }
    }

    public enum DiggingFace {
        BOTTOM(0),
        TOP(1),
        NORTH(2),
        SOUTH(3),
        WEST(4),
        EAST(5),
        SPECIAL(255);


        final byte id;

        DiggingFace(int id) {
            this.id = (byte) id;
        }

        public byte getId() {
            return id;
        }
    }
}
