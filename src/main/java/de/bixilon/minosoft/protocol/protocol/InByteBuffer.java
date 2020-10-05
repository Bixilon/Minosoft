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

package de.bixilon.minosoft.protocol.protocol;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.bixilon.minosoft.game.datatypes.Directions;
import de.bixilon.minosoft.game.datatypes.TextComponent;
import de.bixilon.minosoft.game.datatypes.entities.Location;
import de.bixilon.minosoft.game.datatypes.entities.Poses;
import de.bixilon.minosoft.game.datatypes.entities.meta.EntityMetaData;
import de.bixilon.minosoft.game.datatypes.inventory.Slot;
import de.bixilon.minosoft.game.datatypes.objectLoader.particle.Particle;
import de.bixilon.minosoft.game.datatypes.objectLoader.particle.data.BlockParticleData;
import de.bixilon.minosoft.game.datatypes.objectLoader.particle.data.DustParticleData;
import de.bixilon.minosoft.game.datatypes.objectLoader.particle.data.ItemParticleData;
import de.bixilon.minosoft.game.datatypes.objectLoader.particle.data.ParticleData;
import de.bixilon.minosoft.game.datatypes.objectLoader.recipes.Ingredient;
import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.util.BitByte;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.nbt.tag.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;

public class InByteBuffer {
    final Connection connection;
    final int protocolId;
    final byte[] bytes;
    int position;

    public InByteBuffer(byte[] bytes, Connection connection) {
        this.bytes = bytes;
        this.connection = connection;
        this.protocolId = connection.getVersion().getProtocolVersion();
    }

    public byte[] readByteArray() {
        int count;
        if (protocolId < 19) {
            count = readShort();
        } else {
            count = readVarInt();
        }
        return readBytes(count);
    }

    public short readShort() {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.put(readBytes(Short.BYTES));
        return buffer.getShort(0);
    }

    public Long readLong() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(readBytes(Long.BYTES));
        return buffer.getLong(0);
    }

    public byte[] readBytes(int count) {
        byte[] ret = new byte[count];
        System.arraycopy(bytes, position, ret, 0, count);
        position = position + count;
        return ret;
    }

    public double readFixedPointNumberInteger() {
        return readInt() / 32.0D;
    }

    public long readVarLong() {
        int numRead = 0;
        long result = 0;
        byte read;
        do
        {
            read = readByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 10) {
                throw new RuntimeException("VarLong is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

    public boolean readBoolean() {
        boolean ret;
        ret = readByte() == 1;
        return ret;
    }

    public short[] readLEShorts(int num) {
        short[] ret = new short[num];
        for (int i = 0; i < ret.length; i++) {
            ret[i] = (short) (readByte() & 0xFF);
            ret[i] |= (readByte() & 0xFF) << 8;
        }
        return ret;
    }

    public Float readFloat() {
        ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES);
        buffer.put(readBytes(Float.BYTES));
        return buffer.getFloat(0);
    }

    public String[] readStringArray(int length) {
        String[] ret = new String[length];
        for (int i = 0; i < length; i++) {
            ret[i] = new String(readBytes(readVarInt()), StandardCharsets.UTF_8);
        }
        return ret;
    }

    public String readString(int length) {
        return new String(readBytes(length));
    }

    public UUID readUUID() {
        return new UUID(readLong(), readLong());
    }

    public String readString() {
        int length = readVarInt();
        return new String(readBytes(length), StandardCharsets.UTF_8);
    }

    public int readVarInt() {
        // thanks https://wiki.vg/Protocol#VarInt_and_VarLong
        int numRead = 0;
        int result = 0;
        byte read;
        do
        {
            read = readByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) {
                throw new RuntimeException("VarInt is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }

    public int readInt() {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(readBytes(Integer.BYTES));
        return buffer.getInt(0);
    }

    public double readFixedPointNumberByte() {
        return readByte() / 32.0D;
    }

    public JsonObject readJSON() {
        return JsonParser.parseString(readString()).getAsJsonObject();
    }

    public byte readByte() {
        return bytes[position++];
    }

    public BlockPosition readPosition() {
        //ToDo: protocol id 7
        if (protocolId < 440) {
            long raw = readLong();
            int x = (int) (raw >> 38);
            short y = (short) ((raw >> 26) & 0xFFF);
            int z = (int) (raw & 0x3FFFFFF);
            return new BlockPosition(x, y, z);
        }
        long raw = readLong();
        int x = (int) (raw >> 38);
        short y = (short) (raw & 0xFFF);
        int z = (int) (raw << 26 >> 38);
        return new BlockPosition(x, y, z);
    }

    public TextComponent readTextComponent() {
        return new TextComponent(readString());
    }

    public int getLength() {
        return bytes.length;
    }

    public Directions readDirection() {
        return Directions.byId(readVarInt());
    }

    public Poses readPose() {
        return Poses.byId(readVarInt());
    }

    public ParticleData readParticle() {
        Particle type = connection.getMapping().getParticleById(readVarInt());
        return readParticleData(type);
    }

    public ParticleData readParticleData(Particle type) {
        if (protocolId < 343) {
            // old particle format
            return switch (type.getIdentifier()) {
                case "iconcrack" -> new ItemParticleData(new Slot(connection.getMapping().getItemByLegacy(readVarInt(), readVarInt())), type);
                case "blockcrack", "blockdust", "falling_dust" -> new BlockParticleData(connection.getMapping().getBlockById(readVarInt() << 4), type);
                default -> new ParticleData(type);
            };
        }
        return switch (type.getIdentifier()) {
            case "block", "falling_dust" -> new BlockParticleData(connection.getMapping().getBlockById(readVarInt()), type);
            case "dust" -> new DustParticleData(readFloat(), readFloat(), readFloat(), readFloat(), type);
            case "item" -> new ItemParticleData(readSlot(), type);
            default -> new ParticleData(type);
        };
    }

    public NBTTag readNBT(boolean compressed) {
        if (compressed) {
            short length = readShort();
            if (length == -1) {
                // no nbt data here...
                return new CompoundTag();
            }
            try {
                return new InByteBuffer(Util.decompressGzip(readBytes(length)), connection).readNBT();
            } catch (IOException e) {
                // oh no
                e.printStackTrace();
                throw new IllegalArgumentException("Bad nbt");
            }
        }
        TagTypes type = TagTypes.getById(readByte());
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
        if (protocolId < 402) {
            short id = readShort();
            if (id == -1) {
                return null;
            }
            byte count = readByte();
            short metaData = 0;

            if (protocolId < ProtocolDefinition.FLATTING_VERSION_ID) {
                metaData = readShort();
            }
            CompoundTag nbt = (CompoundTag) readNBT(protocolId < 28);
            return new Slot(connection.getMapping(), connection.getMapping().getItemByLegacy(id, metaData), count, metaData, nbt);
        }
        if (readBoolean()) {
            return new Slot(connection.getMapping(), connection.getMapping().getItemById(readVarInt()), readByte(), (CompoundTag) readNBT());
        }
        return null;
    }

    public String getBase64() {
        return getBase64(getPosition(), getBytesLeft());
    }

    public String getBase64(int pos, int length) {
        return new String(Base64.getEncoder().encode(readBytes(pos, length)));
    }

    public int getPosition() {
        return this.position;
    }

    public void setPosition(int pos) {
        this.position = pos;
    }

    public int getBytesLeft() {
        return bytes.length - position;
    }

    byte[] readBytes(int pos, int count) {
        byte[] ret = new byte[count];
        System.arraycopy(bytes, pos, ret, 0, count);
        return ret;
    }

    public short readAngle() {
        return (short) (readByte() * ProtocolDefinition.ANGLE_CALCULATION_CONSTANT);
    }

    public Location readLocation() {
        return new Location(readDouble(), readDouble(), readDouble());
    }

    public Double readDouble() {
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
        buffer.put(readBytes(Double.BYTES));
        return buffer.getDouble(0);
    }

    public BlockPosition readBlockPosition() {
        return new BlockPosition(readInt(), BitByte.byteToUShort(readByte()), readInt());
    }

    public BlockPosition readBlockPositionInteger() {
        return new BlockPosition(readInt(), (short) (readInt()), readInt());
    }

    public BlockPosition readBlockPositionShort() {
        return new BlockPosition(readInt(), readShort(), readInt());
    }

    public byte[] readBytesLeft() {
        return readBytes(getBytesLeft());
    }

    public int[] readIntArray(int length) {
        int[] ret = new int[length];
        for (int i = 0; i < length; i++) {
            ret[i] = readInt();
        }
        return ret;
    }

    public long[] readLongArray(int length) {
        long[] ret = new long[length];
        for (int i = 0; i < length; i++) {
            ret[i] = readLong();
        }
        return ret;
    }

    public int getProtocolId() {
        return protocolId;
    }

    public EntityMetaData.MetaDataHashMap readMetaData() {
        EntityMetaData.MetaDataHashMap sets = new EntityMetaData.MetaDataHashMap();

        if (protocolId < 48) {
            byte item = readByte();

            while (item != 0x7F) {
                byte index = (byte) (item & 0x1F);
                EntityMetaData.EntityMetaDataValueTypes type = EntityMetaData.EntityMetaDataValueTypes.byId((item & 0xFF) >> 5, protocolId);
                sets.put((int) index, EntityMetaData.getData(type, this));
                item = readByte();
            }
        } else if (protocolId < 107) {
            byte index = readByte();
            while (index != (byte) 0xFF) {
                EntityMetaData.EntityMetaDataValueTypes type = EntityMetaData.EntityMetaDataValueTypes.byId(readByte(), protocolId);
                sets.put((int) index, EntityMetaData.getData(type, this));
                index = readByte();
            }
        } else {
            byte index = readByte();
            while (index != (byte) 0xFF) {
                EntityMetaData.EntityMetaDataValueTypes type = EntityMetaData.EntityMetaDataValueTypes.byId(readVarInt(), protocolId);
                sets.put((int) index, EntityMetaData.getData(type, this));
                index = readByte();
            }
        }
        return sets;
    }

    @Override
    public String toString() {
        return "dataLen: " + bytes.length + "; position: " + position;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public int[] readVarIntArray(int length) {
        int[] ret = new int[length];
        for (int i = 0; i < length; i++) {
            ret[i] = readVarInt();
        }
        return ret;
    }

    public Ingredient readIngredient() {
        return new Ingredient(readSlotArray(readVarInt()));
    }

    public Ingredient[] readIngredientArray(int length) {
        Ingredient[] ret = new Ingredient[length];
        for (int i = 0; i < length; i++) {
            ret[i] = readIngredient();
        }
        return ret;
    }

    public Slot[] readSlotArray(int length) {
        Slot[] res = new Slot[length];
        for (int i = 0; i < length; i++) {
            res[i] = readSlot();
        }
        return res;
    }

    public Connection getConnection() {
        return connection;
    }

    public int readEntityId() {
        if (protocolId < 7) {
            return readInt();
        }
        return readVarInt();
    }
}
