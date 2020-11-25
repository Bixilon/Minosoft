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

package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.data.entities.block.BlockEntityMetaData;
import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.data.world.Chunk;
import de.bixilon.minosoft.data.world.ChunkLocation;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.util.ChunkUtil;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;

import java.util.HashMap;

public class PacketChunkData implements ClientboundPacket {
    final HashMap<BlockPosition, BlockEntityMetaData> blockEntities = new HashMap<>();
    ChunkLocation location;
    Chunk chunk;
    CompoundTag heightMap;
    int[] biomes;
    boolean ignoreOldData;

    @Override
    public boolean read(InByteBuffer buffer) {
        boolean containsSkyLight = buffer.getConnection().getPlayer().getWorld().getDimension().hasSkyLight();
        if (buffer.getVersionId() < 23) {
            this.location = new ChunkLocation(buffer.readInt(), buffer.readInt());
            boolean groundUpContinuous = buffer.readBoolean();
            int sectionBitMask = buffer.readUnsignedShort();
            int addBitMask = buffer.readUnsignedShort();

            // decompress chunk data
            InByteBuffer decompressed;
            if (buffer.getVersionId() < 27) {
                decompressed = Util.decompress(buffer.readBytes(buffer.readInt()), buffer.getConnection());
            } else {
                decompressed = buffer;
            }

            chunk = ChunkUtil.readChunkPacket(decompressed, sectionBitMask, addBitMask, groundUpContinuous, containsSkyLight);
            return true;
        }
        if (buffer.getVersionId() < 62) { // ToDo: was this really changed in 62?
            this.location = new ChunkLocation(buffer.readInt(), buffer.readInt());
            boolean groundUpContinuous = buffer.readBoolean();
            int sectionBitMask;
            if (buffer.getVersionId() < 60) {
                sectionBitMask = buffer.readUnsignedShort();
            } else {
                sectionBitMask = buffer.readInt();
            }
            int size = buffer.readVarInt();
            int lastPos = buffer.getPosition();
            chunk = ChunkUtil.readChunkPacket(buffer, sectionBitMask, 0, groundUpContinuous, containsSkyLight);
            buffer.setPosition(size + lastPos);
            return true;
        }
        this.location = new ChunkLocation(buffer.readInt(), buffer.readInt());
        boolean groundUpContinuous = true; // ToDo: how should we handle this now?
        if (buffer.getVersionId() < 758) {
            groundUpContinuous = buffer.readBoolean();
        }
        if (buffer.getVersionId() >= 732 && buffer.getVersionId() < 746) {
            this.ignoreOldData = buffer.readBoolean();
        }
        int sectionBitMask;
        if (buffer.getVersionId() < 70) {
            sectionBitMask = buffer.readInt();
        } else {
            sectionBitMask = buffer.readVarInt();
        }
        if (buffer.getVersionId() >= 443) {
            heightMap = (CompoundTag) buffer.readNBT();
        }
        if (groundUpContinuous) {
            if (buffer.getVersionId() >= 740) {
                biomes = buffer.readVarIntArray(buffer.readVarInt());
            } else if (buffer.getVersionId() >= 552) {
                biomes = buffer.readIntArray(1024);
            }
        }
        int size = buffer.readVarInt();
        int lastPos = buffer.getPosition();

        if (size > 0) {
            chunk = ChunkUtil.readChunkPacket(buffer, sectionBitMask, 0, groundUpContinuous, containsSkyLight);
            // set position of the byte buffer, because of some reasons HyPixel makes some weird stuff and sends way to much 0 bytes. (~ 190k), thanks @pokechu22
            buffer.setPosition(size + lastPos);
        }
        if (buffer.getVersionId() >= 110) {
            int blockEntitiesCount = buffer.readVarInt();
            for (int i = 0; i < blockEntitiesCount; i++) {
                CompoundTag tag = (CompoundTag) buffer.readNBT();
                BlockEntityMetaData data = BlockEntityMetaData.getData(null, tag);
                if (data == null) {
                    continue;
                }
                blockEntities.put(new BlockPosition(tag.getIntTag("x").getValue(), (short) tag.getIntTag("y").getValue(), tag.getIntTag("z").getValue()), data);
            }
        }
        return true;
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Chunk packet received (chunk: %s)", location));
    }

    public ChunkLocation getLocation() {
        return location;
    }

    public Chunk getChunk() {
        return chunk;
    }

    public HashMap<BlockPosition, BlockEntityMetaData> getBlockEntities() {
        return blockEntities;
    }

    public CompoundTag getHeightMap() {
        return heightMap;
    }
}
