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
import de.bixilon.minosoft.data.mappings.tweaker.VersionTweaker;
import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.data.world.Chunk;
import de.bixilon.minosoft.data.world.ChunkLocation;
import de.bixilon.minosoft.modding.event.events.BlockEntityMetaDataChangeEvent;
import de.bixilon.minosoft.modding.event.events.ChunkDataChangeEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InByteBuffer;
import de.bixilon.minosoft.util.ChunkUtil;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.logging.Log;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;

import java.util.HashMap;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PacketChunkData extends ClientboundPacket {
    private final HashMap<BlockPosition, BlockEntityMetaData> blockEntities = new HashMap<>();
    private ChunkLocation location;
    private Chunk chunk;
    private CompoundTag heightMap;
    private int[] biomes;
    private boolean ignoreOldData;

    @Override
    public boolean read(InByteBuffer buffer) {
        boolean containsSkyLight = buffer.getConnection().getPlayer().getWorld().getDimension().hasSkyLight();
        this.location = new ChunkLocation(buffer.readInt(), buffer.readInt());

        boolean groundUpContinuous = true; // ToDo: how should we handle this now?
        if (buffer.getVersionId() < V_20W45A) {
            groundUpContinuous = buffer.readBoolean();
        }

        if (buffer.getVersionId() < V_14W26A) {
            long[] sectionBitMasks = {buffer.readUnsignedShort()};
            int addBitMask = buffer.readUnsignedShort();

            // decompress chunk data
            InByteBuffer decompressed;
            if (buffer.getVersionId() < V_14W28A) {
                decompressed = Util.decompress(buffer.readBytes(buffer.readInt()), buffer.getConnection());
            } else {
                decompressed = buffer;
            }

            this.chunk = ChunkUtil.readChunkPacket(decompressed, sectionBitMasks, addBitMask, groundUpContinuous, containsSkyLight);
            return true;
        }
        long[] sectionBitMasks;
        if (buffer.getVersionId() < V_15W34C) {
            sectionBitMasks = new long[]{buffer.readUnsignedShort()};
        } else if (buffer.getVersionId() < V_15W36D) {
            sectionBitMasks = new long[]{buffer.readInt()};
        } else if (buffer.getVersionId() < V_21W03A) {
            sectionBitMasks = new long[]{buffer.readVarInt()};
        } else {
            sectionBitMasks = buffer.readLongArray();
        }

        if (buffer.getVersionId() >= V_1_16_PRE7 && buffer.getVersionId() < V_1_16_2_PRE2) {
            this.ignoreOldData = buffer.readBoolean();
        }

        if (buffer.getVersionId() >= V_18W44A) {
            this.heightMap = (CompoundTag) buffer.readNBT();
        }
        if (groundUpContinuous) {
            if (buffer.getVersionId() >= V_20W28A) {
                this.biomes = buffer.readVarIntArray();
            } else if (buffer.getVersionId() >= V_19W36A) {
                this.biomes = buffer.readIntArray(1024);
            }
        }

        int size = buffer.readVarInt();
        int lastPos = buffer.getPosition();


        if (size > 0) {
            this.chunk = ChunkUtil.readChunkPacket(buffer, sectionBitMasks, 0, groundUpContinuous, containsSkyLight);
            // set position of the byte buffer, because of some reasons HyPixel makes some weird stuff and sends way to much 0 bytes. (~ 190k), thanks @pokechu22
            buffer.setPosition(size + lastPos);
        }
        if (buffer.getVersionId() >= V_1_9_4) {
            int blockEntitiesCount = buffer.readVarInt();
            for (int i = 0; i < blockEntitiesCount; i++) {
                CompoundTag tag = (CompoundTag) buffer.readNBT();
                BlockEntityMetaData data = BlockEntityMetaData.getData(null, tag);
                if (data == null) {
                    continue;
                }
                this.blockEntities.put(new BlockPosition(tag.getIntTag("x").getValue(), (short) tag.getIntTag("y").getValue(), tag.getIntTag("z").getValue()), data);
            }
        }
        return true;
    }

    @Override
    public void handle(Connection connection) {
        getBlockEntities().forEach(((position, compoundTag) -> connection.fireEvent(new BlockEntityMetaDataChangeEvent(connection, position, null, compoundTag))));
        VersionTweaker.transformChunk(this.chunk, connection.getVersion().getVersionId());

        connection.fireEvent(new ChunkDataChangeEvent(connection, this));

        connection.getPlayer().getWorld().setChunk(getLocation(), getChunk());
        connection.getPlayer().getWorld().setBlockEntityData(getBlockEntities());
        connection.getRenderer().prepareChunk(this.location, this.chunk);
    }

    @Override
    public void log() {
        Log.protocol(String.format("[IN] Chunk packet received (chunk: %s)", this.location));
    }

    public ChunkLocation getLocation() {
        return this.location;
    }

    public Chunk getChunk() {
        return this.chunk;
    }

    public HashMap<BlockPosition, BlockEntityMetaData> getBlockEntities() {
        return this.blockEntities;
    }

    public CompoundTag getHeightMap() {
        return this.heightMap;
    }
}
