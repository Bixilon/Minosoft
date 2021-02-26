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

package de.bixilon.minosoft.protocol.protocol;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.bixilon.minosoft.data.Directions;
import de.bixilon.minosoft.data.commands.CommandArgumentNode;
import de.bixilon.minosoft.data.commands.CommandLiteralNode;
import de.bixilon.minosoft.data.commands.CommandNode;
import de.bixilon.minosoft.data.commands.CommandRootNode;
import de.bixilon.minosoft.data.entities.EntityMetaData;
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.data.entities.Poses;
import de.bixilon.minosoft.data.inventory.Slot;
import de.bixilon.minosoft.data.mappings.LegacyResourceLocation;
import de.bixilon.minosoft.data.mappings.ResourceLocation;
import de.bixilon.minosoft.data.mappings.biomes.Biome;
import de.bixilon.minosoft.data.mappings.particle.Particle;
import de.bixilon.minosoft.data.mappings.particle.data.BlockParticleData;
import de.bixilon.minosoft.data.mappings.particle.data.DustParticleData;
import de.bixilon.minosoft.data.mappings.particle.data.ItemParticleData;
import de.bixilon.minosoft.data.mappings.particle.data.ParticleData;
import de.bixilon.minosoft.data.mappings.recipes.Ingredient;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.data.world.ChunkLocation;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.nbt.tag.*;
import org.checkerframework.common.value.qual.IntRange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

import static de.bixilon.minosoft.protocol.protocol.ProtocolVersions.*;

public class InByteBuffer {
    private final Connection connection;
    private final int versionId;
    private final byte[] bytes;
    int position;

    public InByteBuffer(byte[] bytes, Connection connection) {
        this.bytes = bytes;
        this.connection = connection;
        this.versionId = connection.getVersion().getVersionId();
    }

    public InByteBuffer(InByteBuffer buffer) {
        this.bytes = buffer.getBytes();
        this.position = buffer.getPosition();
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

    public short readShort() {
        return (short) (((readUnsignedByte()) << 8) | (readUnsignedByte()));
    }

    @IntRange(from = 0, to = ((int) Short.MAX_VALUE) * 2)
    public int readUnsignedShort() {
        return readShort() & 0xFFFF;
    }

    public int readInt() {
        return ((readUnsignedByte() << 24) | (readUnsignedByte() << 16) | (readUnsignedByte() << 8) | (readUnsignedByte()));
    }

    public byte[] readBytes(int count) {
        if (count > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            throw new IllegalArgumentException("Trying to allocate to much memory");
        }
        byte[] ret = new byte[count];
        System.arraycopy(this.bytes, this.position, ret, 0, count);
        this.position += count;
        return ret;
    }

    public long readLong() {
        return (((long) readUnsignedByte() << 56) | ((long) readUnsignedByte() << 48) | ((long) readUnsignedByte() << 40) | ((long) readUnsignedByte() << 32) | ((long) readUnsignedByte() << 24) | (readUnsignedByte() << 16) | (readUnsignedByte() << 8) | (readUnsignedByte()));
    }

    public double readFixedPointNumberInt() {
        return readInt() / 32.0D;
    }

    public String readString() {
        byte[] data = readBytes(readVarInt());
        if (data.length > ProtocolDefinition.STRING_MAX_LEN) {
            throw new IllegalArgumentException(String.format("String max string length exceeded %d > %d", data.length, ProtocolDefinition.STRING_MAX_LEN));
        }
        return new String(data, StandardCharsets.UTF_8);
    }

    public long readVarLong() {
        int byteCount = 0;
        long result = 0;
        byte read;
        do {
            read = readByte();
            result |= (long) (read & 0x7F) << (7 * byteCount);
            byteCount++;
            if (byteCount > 10) {
                throw new IllegalArgumentException("VarLong is too big");
            }
        } while ((read & 0x80) != 0);
        return result;
    }

    public boolean readBoolean() {
        return readByte() == 1;
    }

    public int[] readUnsignedLEShorts(int num) {
        if (num > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            throw new IllegalArgumentException("Trying to allocate to much memory");
        }
        int[] ret = new int[num];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = ((readUnsignedByte()) | (readUnsignedByte() << 8));
        }
        return ret;
    }

    public String[] readStringArray(int length) {
        if (length > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            throw new IllegalArgumentException("Trying to allocate to much memory");
        }
        String[] ret = new String[length];
        for (int i = 0; i < length; i++) {
            ret[i] = readString();
        }
        return ret;
    }

    public String[] readStringArray() {
        return readStringArray(readVarInt());
    }

    public String readString(int length) {
        return new String(readBytes(length));
    }

    public UUID readUUID() {
        return new UUID(readLong(), readLong());
    }

    public int readVarInt() {
        int byteCount = 0;
        int result = 0;
        byte read;
        do {
            read = readByte();
            result |= (read & 0x7F) << (7 * byteCount);
            byteCount++;
            if (byteCount > 5) {
                throw new IllegalArgumentException("VarInt is too big");
            }
        } while ((read & 0x80) != 0);

        return result;
    }

    public double readFixedPointNumberByte() {
        return readByte() / 32.0D;
    }

    public JsonObject readJSON() {
        return JsonParser.parseString(readString()).getAsJsonObject();
    }

    public byte readByte() {
        return this.bytes[this.position++];
    }

    @IntRange(from = 0, to = ((int) Byte.MAX_VALUE) * 2 + 1)
    public short readUnsignedByte() {
        return (short) (this.bytes[this.position++] & 0xFF);
    }

    public BlockPosition readPosition() {
        // ToDo: protocol id 7
        long raw = readLong();
        int x = (int) (raw >> 38);
        if (this.versionId < V_18W43A) {
            int y = (int) ((raw >> 26) & 0xFFF);
            int z = (int) (raw & 0x3FFFFFF);
            return new BlockPosition(x, y, z);
        }
        int y = (int) (raw << 52 >> 52);
        int z = (int) (raw << 26 >> 38);
        return new BlockPosition(x, y, z);
    }

    public ChatComponent readChatComponent() {
        return ChatComponent.valueOf(this.connection.getVersion().getLocaleManager(), readString());
    }

    @IntRange(from = 0)
    public int getLength() {
        return this.bytes.length;
    }

    public Directions readDirection() {
        return Directions.byId(readVarInt());
    }

    public Poses readPose() {
        return Poses.byId(readVarInt());
    }

    public ParticleData readParticle() {
        Particle type = this.connection.getMapping().getParticleRegistry().get(readVarInt());
        return readParticleData(type);
    }

    public ParticleData readParticleData(Particle type) {
        if (this.versionId < V_17W45A) {
            // old particle format
            return switch (type.getResourceLocation().getFull()) {
                case "minecraft:iconcrack" -> new ItemParticleData(new Slot(this.connection.getVersion(), this.connection.getMapping().getItemRegistry().get((readVarInt() << 16) | readVarInt())), type);
                case "minecraft:blockcrack", "minecraft:blockdust", "minecraft:falling_dust" -> new BlockParticleData(this.connection.getMapping().getBlockState(readVarInt() << 4), type);
                default -> new ParticleData(type);
            };
        }
        return switch (type.getResourceLocation().getFull()) {
            case "minecraft:block", "minecraft:falling_dust" -> new BlockParticleData(this.connection.getMapping().getBlockState(readVarInt()), type);
            case "minecraft:dust" -> new DustParticleData(readFloat(), readFloat(), readFloat(), readFloat(), type);
            case "minecraft:item" -> new ItemParticleData(readSlot(), type);
            default -> new ParticleData(type);
        };
    }

    public NBTTag readNBT(boolean compressed) {
        if (compressed) {
            int length = readShort();
            if (length == -1) {
                // no nbt data here...
                return new CompoundTag();
            }
            try {
                return new InByteBuffer(Util.decompressGzip(readBytes(length)), this.connection).readNBT();
            } catch (IOException e) {
                // oh no
                e.printStackTrace();
                throw new IllegalArgumentException("Bad nbt");
            }
        }
        TagTypes type = TagTypes.byId(readUnsignedByte());
        if (type == TagTypes.COMPOUND) {
            // shouldn't be a subtag
            return new CompoundTag(false, this);
        }
        return readNBT(type);
    }

    public NBTTag readNBT(TagTypes tagType) {
        return switch (tagType) {
            case END -> null;
            case BYTE -> new ByteTag(this);
            case SHORT -> new ShortTag(this);
            case INT -> new IntTag(this);
            case LONG -> new LongTag(this);
            case FLOAT -> new FloatTag(this);
            case DOUBLE -> new DoubleTag(this);
            case BYTE_ARRAY -> new ByteArrayTag(this);
            case STRING -> new StringTag(this);
            case LIST -> new ListTag(this);
            case COMPOUND -> new CompoundTag(true, this);
            case INT_ARRAY -> new IntArrayTag(this);
            case LONG_ARRAY -> new LongArrayTag(this);
        };
    }

    public NBTTag readNBT() {
        return readNBT(false);
    }

    public Slot readSlot() {
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
            return new Slot(this.connection.getVersion(), this.connection.getMapping().getItemRegistry().get((id << 16) | metaData), count, metaData, nbt);
        }
        if (readBoolean()) {
            return new Slot(this.connection.getVersion(), this.connection.getMapping().getItemRegistry().get(readVarInt()), readByte(), (CompoundTag) readNBT());
        }
        return null;
    }

    public String getBase64() {
        return getBase64(getPosition(), getBytesLeft());
    }

    public String getBase64(int pos, int length) {
        return new String(Base64.getEncoder().encode(readBytes(pos, length)));
    }

    @IntRange(from = 0, to = ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE)
    public int getPosition() {
        return this.position;
    }

    public void setPosition(int pos) {
        this.position = pos;
    }

    @IntRange(from = 0)
    public int getBytesLeft() {
        return this.bytes.length - this.position;
    }

    byte[] readBytes(int pos, int count) {
        if (count > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            throw new IllegalArgumentException("Trying to allocate to much memory");
        }
        byte[] ret = new byte[count];
        System.arraycopy(this.bytes, pos, ret, 0, count);
        return ret;
    }

    public short readAngle() {
        return (short) (readByte() * ProtocolDefinition.ANGLE_CALCULATION_CONSTANT);
    }

    public Location readLocation() {
        return new Location(readDouble(), readDouble(), readDouble());
    }

    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    public Location readSmallLocation() {
        return new Location(readFloat(), readFloat(), readFloat());
    }

    public float readFloat() {
        return Float.intBitsToFloat(readInt());
    }

    public BlockPosition readBlockPositionByte() {
        return new BlockPosition(readInt(), readUnsignedByte(), readInt());
    }

    public BlockPosition readBlockPositionShort() {
        return new BlockPosition(readInt(), readShort(), readInt());
    }

    public BlockPosition readBlockPositionInteger() {
        return new BlockPosition(readInt(), readInt(), readInt());
    }

    public byte[] readBytesLeft() {
        return readBytes(getBytesLeft());
    }

    public int[] readIntArray(int length) {
        if (length > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            throw new IllegalArgumentException("Trying to allocate to much memory");
        }
        int[] ret = new int[length];
        for (int i = 0; i < length; i++) {
            ret[i] = readInt();
        }
        return ret;
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

    public long[] readLongArray(int length) {
        if (length > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            throw new IllegalArgumentException("Trying to allocate to much memory");
        }
        long[] ret = new long[length];
        for (int i = 0; i < length; i++) {
            ret[i] = readLong();
        }
        return ret;
    }

    public long[] readLongArray() {
        return readLongArray(readVarInt());
    }

    public int getVersionId() {
        return this.versionId;
    }

    public EntityMetaData readMetaData() {
        EntityMetaData metaData = new EntityMetaData(this.connection);
        EntityMetaData.MetaDataHashMap sets = metaData.getSets();

        if (this.versionId < V_15W31A) { // ToDo: This version was 48, but this one does not exist!
            short item = readUnsignedByte();
            while (item != 0x7F) {
                byte index = (byte) (item & 0x1F);
                EntityMetaData.EntityMetaDataValueTypes type = EntityMetaData.EntityMetaDataValueTypes.byId((item & 0xFF) >> 5, this.versionId);
                sets.put((int) index, EntityMetaData.getData(type, this));
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
                EntityMetaData.EntityMetaDataValueTypes type = EntityMetaData.EntityMetaDataValueTypes.byId(id, this.versionId);
                sets.put(index, EntityMetaData.getData(type, this));
                index = readUnsignedByte();
            }
        }
        return metaData;
    }

    @Override
    public String toString() {
        return "dataLen: " + this.bytes.length + "; position: " + this.position;
    }

    public byte[] getBytes() {
        return this.bytes;
    }

    public int[] readVarIntArray(int length) {
        if (length > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            throw new IllegalArgumentException("Trying to allocate to much memory");
        }
        int[] ret = new int[length];
        for (int i = 0; i < length; i++) {
            ret[i] = readVarInt();
        }
        return ret;
    }

    public int[] readVarIntArray() {
        return readVarIntArray(readVarInt());
    }

    public Ingredient readIngredient() {
        return new Ingredient(readSlotArray());
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

    public Slot[] readSlotArray(int length) {
        if (length > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            throw new IllegalArgumentException("Trying to allocate to much memory");
        }
        Slot[] res = new Slot[length];
        for (int i = 0; i < length; i++) {
            res[i] = readSlot();
        }
        return res;
    }

    public Slot[] readSlotArray() {
        return readSlotArray(readVarInt());
    }

    public Connection getConnection() {
        return this.connection;
    }

    public int readEntityId() {
        if (this.versionId < V_14W04A) {
            return readInt();
        }
        return readVarInt();
    }

    public CommandNode[] readCommandNodesArray() {
        int count = readVarInt();

        if (count > ProtocolDefinition.PROTOCOL_PACKET_MAX_SIZE) {
            throw new IllegalArgumentException("Trying to allocate to much memory");
        }
        CommandNode[] nodes = new CommandNode[count];
        for (int i = 0; i < nodes.length; i++) {
            nodes[i] = readCommandNode();
        }
        // resole ids
        for (CommandNode node : nodes) {
            // redirect
            if (node.getRedirectNodeId() != -1) {
                node.setRedirectNode(nodes[node.getRedirectNodeId()]);
            }

            // children
            for (int id : node.getChildrenIds()) {
                CommandNode targetNode = nodes[id];
                if (targetNode instanceof CommandArgumentNode argumentNode) {
                    node.getArgumentsChildren().add(argumentNode);
                } else if (targetNode instanceof CommandLiteralNode literalNode) {
                    node.getLiteralChildren().put(literalNode.getName(), literalNode);
                }
            }
            node.resetChildrenIds();

        }
        return nodes;
    }

    private CommandNode readCommandNode() {
        byte flags = readByte();
        return switch (CommandNode.NodeTypes.byId(flags & 0x03)) {
            case ROOT -> new CommandRootNode(flags, this);
            case LITERAL -> new CommandLiteralNode(flags, this);
            case ARGUMENT -> new CommandArgumentNode(flags, this);
        };
    }

    public ResourceLocation readResourceLocation() {
        String resourceLocation = readString();

        if (Util.doesStringContainsUppercaseLetters(resourceLocation)) {
            // just a string but wrapped into a resourceLocation (like old plugin channels MC|BRAND or ...)
            return new LegacyResourceLocation(resourceLocation);
        }
        return new ResourceLocation(resourceLocation);
    }

    public ChunkLocation readChunkLocation() {
        return new ChunkLocation(readInt(), readInt());
    }
}
