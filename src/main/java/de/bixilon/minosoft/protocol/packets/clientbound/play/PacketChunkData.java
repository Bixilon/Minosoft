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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.world.Chunk;
import de.bixilon.minosoft.game.datatypes.world.ChunkLocation;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;
import de.bixilon.minosoft.util.ChunkUtil;
import de.bixilon.minosoft.util.Util;

public class PacketChunkData implements ClientboundPacket {
    ChunkLocation location;
    Chunk chunk;


    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10: {
                this.location = new ChunkLocation(buffer.readInteger(), buffer.readInteger());
                boolean groundUpContinuous = buffer.readBoolean();
                short sectionBitMask = buffer.readShort();
                short addBitMask = buffer.readShort();

                // decompress chunk data
                InByteBuffer decompressed = Util.decompress(buffer.readBytes(buffer.readInteger()));

                chunk = ChunkUtil.readChunkPacket(v, decompressed, sectionBitMask, addBitMask, groundUpContinuous, true);
                break;
            }
            case VERSION_1_8: {
                this.location = new ChunkLocation(buffer.readInteger(), buffer.readInteger());
                boolean groundUpContinuous = buffer.readBoolean();
                short sectionBitMask = buffer.readShort();
                int size = buffer.readVarInt();

                chunk = ChunkUtil.readChunkPacket(v, buffer, sectionBitMask, (short) 0, groundUpContinuous, true);
                break;
            }
        }

    }

    @Override
    public void log() {
        Log.protocol(String.format("Chunk packet received (chunk: %s)", location.toString()));
    }

    public ChunkLocation getLocation() {
        return location;
    }

    public Chunk getChunk() {
        return chunk;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }
}
