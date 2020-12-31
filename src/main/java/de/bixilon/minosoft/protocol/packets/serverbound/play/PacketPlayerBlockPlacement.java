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

import de.bixilon.minosoft.data.inventory.Slot;
import de.bixilon.minosoft.data.player.Hands;
import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketPlayerBlockPlacement implements ServerboundPacket {
    private final BlockPosition position;
    private final byte direction;
    private final float cursorX;
    private final float cursorY;
    private final float cursorZ;
    Slot item;
    Hands hand;
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
    public PacketPlayerBlockPlacement(BlockPosition position, byte direction, Hands hand, float cursorX, float cursorY, float cursorZ) {
        this.position = position;
        this.direction = direction;
        this.cursorX = cursorX;
        this.cursorY = cursorY;
        this.cursorZ = cursorZ;
    }

    // >= 1.14
    public PacketPlayerBlockPlacement(BlockPosition position, byte direction, Hands hand, float cursorX, float cursorY, float cursorZ, boolean insideBlock) {
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
        if (buffer.getVersionId() >= V_19W03A) {
            buffer.writeVarInt(this.hand.ordinal());
        }
        if (buffer.getVersionId() < V_14W04A) {
            buffer.writeBlockPositionByte(this.position);
        } else {
            buffer.writePosition(this.position);
        }
        if (buffer.getVersionId() < V_15W31A) {
            buffer.writeByte(this.direction);
            buffer.writeSlot(this.item);
        } else {
            buffer.writeVarInt(this.direction);
            if (buffer.getVersionId() < V_19W03A) {
                buffer.writeVarInt(this.hand.ordinal());
            }
        }

        if (buffer.getVersionId() >= V_19W03A) {
            buffer.writeBoolean(this.insideBlock);
        }

        if (buffer.getVersionId() < V_16W39C) {
            buffer.writeByte((byte) (this.cursorX * 15.0F));
            buffer.writeByte((byte) (this.cursorY * 15.0F));
            buffer.writeByte((byte) (this.cursorZ * 15.0F));
        } else {
            buffer.writeFloat(this.cursorX);
            buffer.writeFloat(this.cursorY);
            buffer.writeFloat(this.cursorZ);
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("[OUT] Send player block placement (position=%s, direction=%d, item=%s, cursor=%s %s %s)", this.position, this.direction, this.item, this.cursorX, this.cursorY, this.cursorZ));
    }
}
