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
import de.bixilon.minosoft.data.inventory.Slot;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public class OutByteBuffer {
    final ArrayList<Byte> bytes;
    final Connection connection;
    final int versionId;

    public OutByteBuffer(Connection connection) {
        this.bytes = new ArrayList<>();
        this.connection = connection;
        this.versionId = connection.getVersion().getVersionId();
    }

    public OutByteBuffer(OutByteBuffer buffer) {
        this.bytes = (ArrayList<Byte>) buffer.getBytes().clone();
        this.connection = buffer.getConnection();
        this.versionId = buffer.getVersionId();
    }

    public void writeByteArray(byte[] data) {
        if (versionId < 19) {
            writeShort((short) data.length);
        } else {
            writeVarInt(data.length);
        }
        writeBytes(data);
    }

    public void writeShort(short s) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.putShort(s);
        for (byte b : buffer.array()) {
            bytes.add(b);
        }
    }

    public void writeInt(int i) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(i);
        for (byte b : buffer.array()) {
            bytes.add(b);
        }
    }

    public void writeBytes(byte[] b) {
        for (byte value : b) {
            bytes.add(value);
        }
    }

    public void writeLong(Long l) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(l);
        for (byte b : buffer.array()) {
            bytes.add(b);
        }
    }

    public void writeTextComponent(ChatComponent chatComponent) {
        writeString(chatComponent.getMessage()); //ToDo: test if this should not be json
    }

    public void writeJSON(JsonObject j) {
        writeString(j.toString());
    }

    public void writeString(String s) {
        writeVarInt(s.length());
        for (byte b : s.getBytes(StandardCharsets.UTF_8)) {
            bytes.add(b);
        }
    }

    public void writeVarLong(long value) {
        do
        {
            byte temp = (byte) (value & 0b01111111);
            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            writeByte(temp);
        } while (value != 0);
    }

    public void writeByte(byte b) {
        bytes.add(b);
    }

    public void writeFloat(Float f) {
        ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES);
        buffer.putFloat(f);
        for (byte b : buffer.array()) {
            bytes.add(b);
        }
    }

    public void writeDouble(Double d) {
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
        buffer.putDouble(d);
        for (byte b : buffer.array()) {
            bytes.add(b);
        }
    }

    public void writeUUID(UUID u) {
        ByteBuffer buffer = ByteBuffer.allocate(16); // UUID.BYTES
        buffer.putLong(u.getMostSignificantBits());
        buffer.putLong(u.getLeastSignificantBits());
        for (byte b : buffer.array()) {
            bytes.add(b);
        }
    }

    public void writeFixedPointNumberInteger(double d) {
        writeInt((int) (d * 32.0D));
    }

    public ArrayList<Byte> getBytes() {
        return bytes;
    }

    public void writePosition(BlockPosition position) {
        if (position == null) {
            writeLong(0L);
            return;
        }
        if (versionId < 440) {
            writeLong((((long) position.getX() & 0x3FFFFFF) << 38) | (((long) position.getZ() & 0x3FFFFFF)) | ((long) position.getY() & 0xFFF) << 26);
            return;
        }
        writeLong((((long) (position.getX() & 0x3FFFFFF) << 38) | ((long) (position.getZ() & 0x3FFFFFF) << 12) | (long) (position.getY() & 0xFFF)));
    }

    public void writeVarInt(int value) {
        // thanks https://wiki.vg/Protocol#VarInt_and_VarLong
        do
        {
            byte temp = (byte) (value & 0b01111111);
            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            writeByte(temp);
        } while (value != 0);
    }

    public void prefixVarInt(int value) {
        int count = 0;
        // thanks https://wiki.vg/Protocol#VarInt_and_VarLong
        do
        {
            byte temp = (byte) (value & 0b01111111);
            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            bytes.add(count++, temp);
        } while (value != 0);
    }

    public void writeSlot(Slot slot) {
        if (versionId < 402) {
            if (slot == null) {
                writeShort((short) -1);
                return;
            }
            writeShort((short) (int) connection.getMapping().getItemId(slot.getItem()));
            writeByte((byte) slot.getItemCount());
            writeShort(slot.getItemMetadata());
            writeNBT(slot.getNbt(connection.getMapping()));
        }
        if (slot == null) {
            writeBoolean(false);
            return;
        }
        writeVarInt(connection.getMapping().getItemId(slot.getItem()));
        writeByte((byte) slot.getItemCount());
        writeNBT(slot.getNbt(connection.getMapping()));
    }

    void writeNBT(CompoundTag nbt) {
        // ToDo: test
        nbt.writeBytes(this);
    }

    public void writeBoolean(boolean b) {
        bytes.add((byte) ((b) ? 0x01 : 0x00));
    }

    public void writeStringNoLength(String s) {
        for (byte b : s.getBytes(StandardCharsets.UTF_8)) {
            bytes.add(b);
        }
    }

    public void writeBlockPositionByte(BlockPosition pos) {
        writeInt(pos.getX());
        writeByte((byte) pos.getY());
        writeInt(pos.getZ());
    }

    public byte[] getOutBytes() {
        byte[] ret = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            ret[i] = bytes.get(i);
        }
        return ret;
    }

    public void writeIntegers(int[] data) {
        for (int integer : data) {
            writeInt(integer);
        }
    }

    public void writeLongs(long[] data) {
        for (long long_long : data) {
            writeLong(long_long);
        }
    }

    public int getVersionId() {
        return versionId;
    }

    public void writeEntityId(int entityId) {
        if (versionId < 7) {
            writeInt(entityId);
        } else {
            writeVarInt(entityId);
        }
    }

    public Connection getConnection() {
        return connection;
    }
}
