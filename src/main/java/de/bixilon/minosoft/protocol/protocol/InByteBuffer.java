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

import de.bixilon.minosoft.game.datatypes.Direction;
import de.bixilon.minosoft.game.datatypes.TextComponent;
import de.bixilon.minosoft.game.datatypes.entities.Location;
import de.bixilon.minosoft.game.datatypes.entities.Pose;
import de.bixilon.minosoft.game.datatypes.entities.items.Items;
import de.bixilon.minosoft.game.datatypes.entities.meta.EntityMetaData;
import de.bixilon.minosoft.game.datatypes.inventory.Slot;
import de.bixilon.minosoft.game.datatypes.particle.*;
import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.nbt.tag.CompoundTag;
import de.bixilon.minosoft.util.BitByte;
import de.bixilon.minosoft.util.Util;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

public class InByteBuffer {
    final ProtocolVersion version;
    final byte[] bytes;
    int pos;

    public InByteBuffer(byte[] bytes, ProtocolVersion version) {
        this.bytes = bytes;
        this.version = version;
    }

    public byte readByte() {
        byte ret;
        ret = bytes[pos];
        pos = pos + 1;
        return ret;
    }

    public byte[] readBytes(int count) {
        byte[] ret = new byte[count];
        System.arraycopy(bytes, pos, ret, 0, count);
        pos = pos + count;
        return ret;
    }


    byte[] readBytes(int pos, int count) {
        byte[] ret = new byte[count];
        System.arraycopy(bytes, pos, ret, 0, count);
        return ret;
    }

    public boolean readBoolean() {
        boolean ret;
        ret = readByte() == 1;
        return ret;
    }

    public short readShort() {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.put(readBytes(Short.BYTES));
        return buffer.getShort(0);
    }

    public short[] readShorts(int num) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES * num);
        buffer.put(readBytes(Short.BYTES * num));
        short[] ret = new short[num];
        for (int i = 0; i < num; i++) {
            ret[i] = buffer.getShort(i);
        }
        return ret;
    }

    public int readInt() {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(readBytes(Integer.BYTES));
        return buffer.getInt(0);
    }

    public Long readLong() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(readBytes(Long.BYTES));
        return buffer.getLong(0);
    }

    public Float readFloat() {
        ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES);
        buffer.put(readBytes(Float.BYTES));
        return buffer.getFloat(0);
    }

    public Double readDouble() {
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
        buffer.put(readBytes(Double.BYTES));
        return buffer.getDouble(0);
    }

    public String readString() {
        int length = readVarInt();
        return new String(readBytes(length), StandardCharsets.UTF_8);
    }

    public String readString(int length) {
        return new String(readBytes(length));
    }


    public UUID readUUID() {
        return new UUID(readLong(), readLong());
    }

    public int readVarInt() {
        // thanks https://wiki.vg/Protocol#VarInt_and_VarLong
        int numRead = 0;
        int result = 0;
        byte read;
        do {
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

    public long readVarLong() {
        int numRead = 0;
        long result = 0;
        byte read;
        do {
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

    public double readFixedPointNumberInteger() {
        return readInt() / 32.0D;
    }

    public double readFixedPointNumberByte() {
        return readByte() / 32.0D;
    }

    public JSONObject readJSON() {
        return new JSONObject(readString());
    }

    public BlockPosition readPosition() {
        if (version.getVersionNumber() >= ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            // changed in 1.14, thanks for the explanation @Sainan
            Long raw = readLong();
            int x = (int) (raw >> 38);
            short y = (short) (raw & 0xFFF);
            int z = (int) (raw << 26 >> 38);
            return new BlockPosition(x, y, z);
        }
        Long raw = readLong();
        int x = (int) (raw >> 38);
        short y = (short) ((raw >> 26) & 0xFFF);
        int z = (int) (raw & 0x3FFFFFF);
        return new BlockPosition(x, y, z);
    }

    public TextComponent readTextComponent() {
        return new TextComponent(readString());
    }

    public int getPosition() {
        return this.pos;
    }

    public void setPosition(int pos) {
        this.pos = pos;
    }

    public int getLength() {
        return bytes.length;
    }

    public int getBytesLeft() {
        return bytes.length - pos;
    }

    public Direction readDirection() {
        return Direction.byId(readVarInt());
    }

    public Pose readPose() {
        return Pose.byId(readVarInt());
    }

    public Particle readParticle() {
        Particles type = Particles.byId(readVarInt());
        try {
            if (type.getClazz() == OtherParticles.class) {
                return type.getClazz().getConstructor(Particles.class).newInstance(type);
            } else if (type.getClazz() == BlockParticle.class) {
                return type.getClazz().getConstructor(int.class).newInstance(readVarInt());
            } else if (type.getClazz() == DustParticle.class) {
                return type.getClazz().getConstructor(float.class, float.class, float.class, float.class).newInstance(readFloat(), readFloat(), readFloat(), readFloat());
            } else if (type.getClazz() == FallingDustParticle.class) {
                return type.getClazz().getConstructor(int.class).newInstance(readVarInt());
            } else if (type.getClazz() == ItemParticle.class) {
                return type.getClazz().getConstructor(Slot.class).newInstance(readSlot());
            }
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

    public CompoundTag readNBT(boolean compressed) {
        if (compressed) {
            short length = readShort();
            if (length == -1) {
                // no nbt data here...
                return new CompoundTag();
            }
            try {
                return new CompoundTag(new InByteBuffer(Util.decompressGzip(readBytes(length)), version));
            } catch (IOException e) {
                // oh no
                e.printStackTrace();
                throw new IllegalArgumentException("Bad nbt");
            }
            // try again
        }
        return new CompoundTag(this);
    }

    public CompoundTag readNBT() {
        return readNBT(false);
    }

    public Slot readSlot() {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
                short id = readShort();
                if (id == -1) {
                    return null;
                }
                byte count = readByte();
                short metaData = readShort();
                CompoundTag nbt = readNBT(version == ProtocolVersion.VERSION_1_7_10);
                return new Slot(Items.getItemByLegacy(id, metaData, version), count, metaData, nbt);
            case VERSION_1_13_2:
                if (readBoolean()) {
                    return new Slot(Items.getItem(readVarInt(), version), readByte(), readNBT());
                }
        }
        return null;
    }

    public String getBase64(int pos, int length) {
        return new String(Base64.getEncoder().encode(readBytes(pos, length)));
    }

    public String getBase64() {
        return getBase64(getPosition(), getBytesLeft());
    }

    public short readAngle() {
        return (short) (readByte() * ProtocolDefinition.ANGLE_CALCULATION_CONSTANT);
    }

    public Location readLocation() {
        return new Location(readDouble(), readDouble(), readDouble());
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

    public InPacketBuffer getPacketBuffer() {
        return new InPacketBuffer(this, getVersion());
    }

    public byte[] readBytesLeft() {
        return readBytes(getBytesLeft());
    }

    public int[] readInts(int length) {
        int[] ret = new int[length];
        for (int i = 0; i < length; i++) {
            ret[i] = readInt();
        }
        return ret;
    }

    public long[] readLongs(int length) {
        long[] ret = new long[length];
        for (int i = 0; i < length; i++) {
            ret[i] = readLong();
        }
        return ret;
    }

    public ProtocolVersion getVersion() {
        return version;
    }

    public HashMap<Integer, EntityMetaData.MetaDataSet> readMetaData() {
        HashMap<Integer, EntityMetaData.MetaDataSet> sets = new HashMap<>();

        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8: {
                byte item = readByte();

                while (item != 0x7F) {
                    byte index = (byte) (item & 0x1F);
                    EntityMetaData.Types type = EntityMetaData.Types.byId((item & 0xFF) >> 5, version);
                    sets.put((int) index, new EntityMetaData.MetaDataSet(index, EntityMetaData.getData(type, this)));
                    item = readByte();
                }
                break;
            }
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2: {
                byte index = readByte();
                while (index != (byte) 0xFF) {
                    EntityMetaData.Types type = EntityMetaData.Types.byId(readByte(), version);
                    sets.put((int) index, new EntityMetaData.MetaDataSet(index, EntityMetaData.getData(type, this)));
                    index = readByte();
                }
                break;
            }
            case VERSION_1_12_2:
            case VERSION_1_13_2: {
                byte index = readByte();
                while (index != (byte) 0xFF) {
                    EntityMetaData.Types type = EntityMetaData.Types.byId(readVarInt(), version);
                    sets.put((int) index, new EntityMetaData.MetaDataSet(index, EntityMetaData.getData(type, this)));
                    index = readByte();
                }
                break;
            }
        }
        return sets;
    }

    @Override
    public String toString() {
        return "dataLen: " + bytes.length + "; pos: " + pos;
    }

    public byte[] getBytes() {
        return bytes;
    }
}
