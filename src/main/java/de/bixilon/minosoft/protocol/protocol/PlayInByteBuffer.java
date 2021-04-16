/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.protocol;

import de.bixilon.minosoft.data.entities.meta.EntityMetaData;
import de.bixilon.minosoft.data.inventory.ItemStack;
import de.bixilon.minosoft.data.mappings.biomes.Biome;
import de.bixilon.minosoft.data.mappings.particle.Particle;
import de.bixilon.minosoft.data.mappings.particle.data.BlockParticleData;
import de.bixilon.minosoft.data.mappings.particle.data.DustParticleData;
import de.bixilon.minosoft.data.mappings.particle.data.ItemParticleData;
import de.bixilon.minosoft.data.mappings.particle.data.ParticleData;
import de.bixilon.minosoft.data.mappings.recipes.Ingredient;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;
import de.bixilon.minosoft.util.nbt.tag.NBTTag;
import glm_.vec3.Vec3i;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class PlayInByteBuffer extends InByteBuffer {
    private final PlayConnection connection;
    private final int versionId;

    public PlayInByteBuffer(byte[] bytes, PlayConnection connection) {
        super(bytes, connection);
        this.connection = connection;
        this.versionId = connection.getVersion().getVersionId();
    }

    public PlayInByteBuffer(PlayInByteBuffer buffer) {
        super(buffer);
        this.connection = buffer.getConnection();
        this.versionId = this.connection.getVersion().getVersionId();
    }

    public byte[] readByteArray() {
        int count;
        if (this.versionId < V_14W21A) {
            count = readUnsignedShort();
        } else {
            count = readVarInt();
        }
        return readBytes(count);
    }


    public Vec3i readBlockPosition() {
        // ToDo: protocol id 7
        long raw = readLong();
        int x = (int) (raw >> 38);
        if (this.versionId < V_18W43A) {
            int y = (int) ((raw >> 26) & 0xFFF);
            int z = (int) (raw & 0x3FFFFFF);
            return new Vec3i(x, y, z);
        }
        int y = (int) (raw << 52 >> 52);
        int z = (int) (raw << 26 >> 38);
        return new Vec3i(x, y, z);
    }

    @Override
    public ChatComponent readChatComponent() {
        return ChatComponent.Companion.valueOf(this.connection.getVersion().getLocaleManager(), null, readString());
    }

    public ParticleData readParticle() {
        Particle type = this.connection.getMapping().getParticleRegistry().get(readVarInt());
        return readParticleData(type);
    }

    public ParticleData readParticleData(Particle type) {
        if (this.versionId < V_17W45A) {
            // old particle format
            return switch (type.getResourceLocation().getFull()) {
                case "minecraft:iconcrack" -> new ItemParticleData(new ItemStack(this.connection.getMapping().getItemRegistry().get((readVarInt() << 16) | readVarInt()), this.connection.getVersion()), type);
                case "minecraft:blockcrack", "minecraft:blockdust", "minecraft:falling_dust" -> new BlockParticleData(this.connection.getMapping().getBlockState(readVarInt() << 4), type);
                default -> new ParticleData(type);
            };
        }
        return switch (type.getResourceLocation().getFull()) {
            case "minecraft:block", "minecraft:falling_dust" -> new BlockParticleData(this.connection.getMapping().getBlockState(readVarInt()), type);
            case "minecraft:dust" -> new DustParticleData(readFloat(), readFloat(), readFloat(), readFloat(), type);
            case "minecraft:item" -> new ItemParticleData(readItemStack(), type);
            default -> new ParticleData(type);
        };
    }

    @Override
    public NBTTag readNBT() {
        return readNBT(this.versionId < V_14W28B);
    }

    public ItemStack readItemStack() {
        if (this.versionId < V_1_13_2_PRE1) {
            short id = readShort();
            if (id == -1) {
                return null;
            }
            byte count = readByte();
            short metaData = 0;

            if (this.versionId < ProtocolDefinition.FLATTING_VERSION_ID) {
                metaData = readShort();
            }
            CompoundTag nbt = (CompoundTag) readNBT(this.versionId < V_14W28B);
            return new ItemStack(this.connection.getVersion(), this.connection.getMapping().getItemRegistry().get((id << 16) | metaData), count, metaData, nbt);
        }
        if (readBoolean()) {
            return new ItemStack(this.connection.getVersion(), this.connection.getMapping().getItemRegistry().get(readVarInt()), readByte(), (CompoundTag) readNBT());
        }
        return null;
    }

    public Biome[] readBiomeArray() {
        int length = 0;
        if (this.versionId >= V_20W28A) {
            length = readVarInt();
        } else if (this.versionId >= V_19W36A) {
            length = 1024;
        }
        if (length > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            throw new IllegalArgumentException("Trying to allocate to much memory");
        }

        Biome[] ret = new Biome[length];
        for (int i = 0; i < length; i++) {
            int biomeId;

            if (this.versionId >= V_20W28A) {
                biomeId = readVarInt();
            } else {
                biomeId = readInt();
            }
            ret[i] = this.connection.getMapping().getBiomeRegistry().get(biomeId);
        }
        return ret;
    }

    public int getVersionId() {
        return this.versionId;
    }

    public EntityMetaData readMetaData() {
        EntityMetaData metaData = new EntityMetaData(this.connection);
        EntityMetaData.MetaDataHashMap sets = metaData.getSets();

        if (this.versionId < V_15W31A) { // ToDo: This version was 48, but this one does not exist!
            int item = readUnsignedByte();
            while (item != 0x7F) {
                byte index = (byte) (item & 0x1F);
                EntityMetaData.EntityMetaDataDataTypes type = this.connection.getMapping().getEntityMetaDataDataDataTypesRegistry().get((item & 0xFF) >> 5);
                sets.put((int) index, metaData.getData(type, this));
                item = readByte();
            }
        } else {
            int index = readUnsignedByte();
            while (index != 0xFF) {
                int id;
                if (this.versionId < V_1_9_1_PRE1) {
                    id = readUnsignedByte();
                } else {
                    id = readVarInt();
                }
                EntityMetaData.EntityMetaDataDataTypes type = this.connection.getMapping().getEntityMetaDataDataDataTypesRegistry().get(id);
                if (type == null) {
                    throw new IllegalStateException("Can not get meta data index for id " + id);
                }
                sets.put(index, metaData.getData(type, this));
                index = readUnsignedByte();
            }
        }
        return metaData;
    }

    public Ingredient readIngredient() {
        return new Ingredient(readItemStackArray());
    }

    public Ingredient[] readIngredientArray(int length) {
        if (length > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            throw new IllegalArgumentException("Trying to allocate to much memory");
        }
        Ingredient[] ret = new Ingredient[length];
        for (int i = 0; i < length; i++) {
            ret[i] = readIngredient();
        }
        return ret;
    }

    public Ingredient[] readIngredientArray() {
        return readIngredientArray(readVarInt());
    }

    public ItemStack[] readItemStackArray(int length) {
        if (length > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            throw new IllegalArgumentException("Trying to allocate to much memory");
        }
        ItemStack[] res = new ItemStack[length];
        for (int i = 0; i < length; i++) {
            res[i] = readItemStack();
        }
        return res;
    }

    public ItemStack[] readItemStackArray() {
        return readItemStackArray(readVarInt());
    }

    public PlayConnection getConnection() {
        return this.connection;
    }

    public int readEntityId() {
        if (this.versionId < V_14W04A) {
            return readInt();
        }
        return readVarInt();
    }
}
