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

import de.bixilon.minosoft.game.datatypes.inventory.Slot;
import de.bixilon.minosoft.game.datatypes.player.Hand;
import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;

public class PacketPlayerBlockPlacement implements ServerboundPacket {
    final BlockPosition position;
    final byte direction;
    final float cursorX;
    final float cursorY;
    final float cursorZ;
    Slot item;
    Hand hand;
    boolean insideBlock;

    public PacketPlayerBlockPlacement(BlockPosition position, byte direction, Slot item, float cursorX, float cursorY, float cursorZ) {
        this.position = position;
        this.direction = direction;
        this.item = item;
        this.cursorX = cursorX;
        this.cursorY = cursorY;
        this.cursorZ = cursorZ;
    }

    // >= 1.9
    public PacketPlayerBlockPlacement(BlockPosition position, byte direction, Hand hand, float cursorX, float cursorY, float cursorZ) {
        this.position = position;
        this.direction = direction;
        this.cursorX = cursorX;
        this.cursorY = cursorY;
        this.cursorZ = cursorZ;
    }

    // >= 1.14
    public PacketPlayerBlockPlacement(BlockPosition position, byte direction, Hand hand, float cursorX, float cursorY, float cursorZ, boolean insideBlock) {
        this.position = position;
        this.direction = direction;
        this.hand = hand;
        this.cursorX = cursorX;
        this.cursorY = cursorY;
        this.cursorZ = cursorZ;
        this.insideBlock = insideBlock;
    }

    @Override
    public OutPacketBuffer write(Connection connection) {
        OutPacketBuffer buffer = new OutPacketBuffer(connection, Packets.Serverbound.PLAY_PLAYER_BLOCK_PLACEMENT);
        if (buffer.getProtocolId() >= 453) {
            buffer.writeVarInt(hand.getId());
        }
        if (buffer.getProtocolId() < 7) {
            buffer.writeBlockPositionByte(position);
        } else {
            buffer.writePosition(position);
        }
        if (buffer.getProtocolId() < 49) {
            buffer.writeByte(direction);
            buffer.writeSlot(item);
        } else {
            buffer.writeVarInt(direction);
            if (buffer.getProtocolId() < 453) {
                buffer.writeVarInt(hand.getId());
            }
        }

        if (buffer.getProtocolId() >= 453) {
            buffer.writeBoolean(insideBlock);
        }

        if (buffer.getProtocolId() < 309) {
            buffer.writeByte((byte) (cursorX * 15.0F));
            buffer.writeByte((byte) (cursorY * 15.0F));
            buffer.writeByte((byte) (cursorZ * 15.0F));
        } else {
            buffer.writeFloat(cursorX);
            buffer.writeFloat(cursorY);
            buffer.writeFloat(cursorZ);
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Send player block placement (position=%s, direction=%d, item=%s, cursor=%s %s %s)", position, direction, item, cursorX, cursorY, cursorZ));
    }
}
