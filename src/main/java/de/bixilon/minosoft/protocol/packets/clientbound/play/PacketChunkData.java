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

import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.game.datatypes.world.Chunk;
import de.bixilon.minosoft.game.datatypes.world.ChunkLocation;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.nbt.tag.CompoundTag;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.util.ChunkUtil;
import de.bixilon.minosoft.util.Util;

import java.util.HashMap;

public class PacketChunkData implements ClientboundPacket {
    ChunkLocation location;
    Chunk chunk;

    HashMap<BlockPosition, CompoundTag> blockEntities = new HashMap<>();

    @Override
    public boolean read(InPacketBuffer buffer) {
        switch (buffer.getVersion()) {
            case VERSION_1_7_10: {
                this.location = new ChunkLocation(buffer.readInt(), buffer.readInt());
                boolean groundUpContinuous = buffer.readBoolean();
                short sectionBitMask = buffer.readShort();
                short addBitMask = buffer.readShort();

                // decompress chunk data
                InByteBuffer decompressed = Util.decompress(buffer.readBytes(buffer.readInt()), buffer.getVersion());

                chunk = ChunkUtil.readChunkPacket(decompressed, sectionBitMask, addBitMask, groundUpContinuous, true);
                return true;
            }
            case VERSION_1_8: {
                this.location = new ChunkLocation(buffer.readInt(), buffer.readInt());
                boolean groundUpContinuous = buffer.readBoolean();
                short sectionBitMask = buffer.readShort();
                int size = buffer.readVarInt();
                int lastPos = buffer.getPosition();
                buffer.setPosition(size + lastPos);

                chunk = ChunkUtil.readChunkPacket(buffer, sectionBitMask, (short) 0, groundUpContinuous, true);
                return true;
            }
            case VERSION_1_9_4: {
                this.location = new ChunkLocation(buffer.readInt(), buffer.readInt());
                boolean groundUpContinuous = buffer.readBoolean();
                short sectionBitMask = (short) buffer.readVarInt();
                int size = buffer.readVarInt();
                int lastPos = buffer.getPosition();

                chunk = ChunkUtil.readChunkPacket(buffer, sectionBitMask, (short) 0, groundUpContinuous, true);
                // set position of the byte buffer, because of some reasons HyPixel makes some weired stuff and sends way to much 0 bytes. (~ 190k)
                buffer.setPosition(size + lastPos);
                int blockEntitiesCount = buffer.readVarInt();
                for (int i = 0; i < blockEntitiesCount; i++) {
                    CompoundTag tag = buffer.readNBT();
                    blockEntities.put(new BlockPosition(tag.getIntTag("x").getValue(), (short) tag.getIntTag("y").getValue(), tag.getIntTag("z").getValue()), tag);
                }
                return true;
            }
        }


        return false;
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

    public HashMap<BlockPosition, CompoundTag> getBlockEntities() {
        return blockEntities;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }
}
