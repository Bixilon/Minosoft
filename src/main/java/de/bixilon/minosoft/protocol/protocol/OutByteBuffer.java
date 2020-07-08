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

import de.bixilon.minosoft.game.datatypes.TextComponent;
import de.bixilon.minosoft.game.datatypes.inventory.Slot;
import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.nbt.tag.CompoundTag;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OutByteBuffer {
    final List<Byte> bytes = new ArrayList<>();
    final ProtocolVersion version;

    public OutByteBuffer(ProtocolVersion version) {
        this.version = version;
    }

    public static void writeByte(byte b, List<Byte> write) {
        write.add(b);
    }

    public static void writeVarInt(int value, List<Byte> write) {
        // thanks https://wiki.vg/Protocol#VarInt_and_VarLong
        do {
            byte temp = (byte) (value & 0b01111111);
            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            writeByte(temp, write);
        } while (value != 0);
    }

    public void writeByte(byte b) {
        bytes.add(b);
    }

    public void writeBytes(byte[] b) {
        for (byte value : b) {
            bytes.add(value);
        }
    }

    public void writeBoolean(boolean b) {
        bytes.add((byte) ((b) ? 0x01 : 0x00));
    }

    public void writeShort(short s) {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.putShort(s);
        for (byte b : buffer.array()) {
            bytes.add(b);
        }
    }

    public void writeLong(Long l) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(l);
        for (byte b : buffer.array()) {
            bytes.add(b);
        }
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

    public void writeString(String s) {
        writeVarInt(s.length());
        for (byte b : s.getBytes(StandardCharsets.UTF_8)) {
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

    public void writeInt(int i) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(i);
        for (byte b : buffer.array()) {
            bytes.add(b);
        }
    }

    public void writeVarInt(int value) {
        writeVarInt(value, bytes);
    }

    public void writeVarLong(long value) {
        do {
            byte temp = (byte) (value & 0b01111111);
            // Note: >>> means that the sign bit is shifted with the rest of the number rather than being left alone
            value >>>= 7;
            if (value != 0) {
                temp |= 0b10000000;
            }
            writeByte(temp);
        } while (value != 0);
    }

    public void writeFixedPointNumberInteger(double d) {
        writeInt((int) (d * 32.0D));
    }

    public void writeFixedPointNumberByte(double d) {
        writeInt((int) (d * 32.0D));
    }

    public List<Byte> getBytes() {
        return bytes;
    }

    public void writeJSON(JSONObject j) {
        writeString(j.toString());
    }

    public void writePosition(BlockPosition location) {
        if (version.getVersionNumber() >= ProtocolVersion.VERSION_1_14_4.getVersionNumber()) {
            writeLong((((long) (location.getX() & 0x3FFFFFF) << 38) | ((long) (location.getZ() & 0x3FFFFFF) << 12) | (long) (location.getY() & 0xFFF)));
        } else {
            writeLong((((long) location.getX() & 0x3FFFFFF) << 38) | (((long) location.getZ() & 0x3FFFFFF)) | ((long) location.getY() & 0xFFF) << 26);
        }
    }

    public void writeTextComponent(TextComponent component) {
        writeJSON(component.getRaw());
    }

    public void writeSlot(Slot slot) {
        switch (version) {
            case VERSION_1_7_10:
            case VERSION_1_8:
            case VERSION_1_9_4:
            case VERSION_1_10:
            case VERSION_1_11_2:
            case VERSION_1_12_2:
                if (slot == null) {
                    writeShort((short) -1);
                    return;
                }
                writeShort((short) slot.getItemId());
                writeByte((byte) slot.getItemCount());
                writeShort(slot.getItemMetadata());
                writeNBT(slot.getNbt());
                break;
            case VERSION_1_13_2:
                if (slot == null) {
                    writeBoolean(false);
                    return;
                }
                writeVarInt(slot.getItemId());
                writeByte((byte) slot.getItemCount());
                writeNBT(slot.getNbt());

        }
    }

    void writeNBT(CompoundTag nbt) {
        // ToDo: test
        nbt.writeBytes(this);
    }

    public void writeStringNoLength(String s) {
        for (byte b : s.getBytes(StandardCharsets.UTF_8)) {
            bytes.add(b);
        }
    }

    public void writeBlockPositionInteger(BlockPosition pos) {
        writeInt(pos.getX());
        writeInt(pos.getY());
        writeInt(pos.getZ());
    }

    public void writeBlockPositionShort(BlockPosition pos) {
        writeInt(pos.getX());
        writeShort((short) pos.getY());
        writeInt(pos.getZ());
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
}
