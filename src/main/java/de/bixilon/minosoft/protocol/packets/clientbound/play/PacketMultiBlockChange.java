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

import de.bixilon.minosoft.game.datatypes.blocks.Blocks;
import de.bixilon.minosoft.game.datatypes.world.ChunkLocation;
import de.bixilon.minosoft.game.datatypes.world.InChunkLocation;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.HashMap;

public class PacketMultiBlockChange implements ClientboundPacket {
    final HashMap<InChunkLocation, Blocks> blocks = new HashMap<>();
    ChunkLocation location;

    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10: {
                location = new ChunkLocation(buffer.readInteger(), buffer.readInteger());
                short count = buffer.readShort();
                int dataSize = buffer.readInteger(); // should be count * 4
                if (dataSize != count * 4) {
                    throw new IllegalArgumentException(String.format("Not enough data (%d) for %d blocks", dataSize, count));
                }
                for (int i = 0; i < count; i++) {
                    int raw = buffer.readInteger();
                    byte meta = (byte) (raw & 0xF);
                    short blockId = (short) ((raw & 0xFF_F0) >>> 4);
                    byte y = (byte) ((raw & 0xFF_00_00) >>> 16);
                    byte z = (byte) ((raw & 0x0F_00_00_00) >>> 24);
                    byte x = (byte) ((raw & 0xF0_00_00_00) >>> 28);
                    blocks.put(new InChunkLocation(x, y, z), Blocks.byLegacy(blockId, meta));
                }
                break;
            }
            case VERSION_1_8: {
                location = new ChunkLocation(buffer.readInteger(), buffer.readInteger());
                int count = buffer.readVarInt();
                for (int i = 0; i < count; i++) {
                    byte pos = buffer.readByte();
                    byte y = buffer.readByte();
                    int blockId = buffer.readVarInt();
                    blocks.put(new InChunkLocation(((pos & 0xF0) >>> 4), y, (pos & 0xF)), Blocks.byLegacy((blockId >>> 4), (blockId & 0xF)));
                }
                break;
            }
        }
    }

    @Override
    public void log() {
        Log.protocol(String.format("Multi block change received at %s (size=%d)", location.toString(), blocks.size()));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public ChunkLocation getLocation() {
        return location;
    }

    public HashMap<InChunkLocation, Blocks> getBlocks() {
        return blocks;
    }
}
