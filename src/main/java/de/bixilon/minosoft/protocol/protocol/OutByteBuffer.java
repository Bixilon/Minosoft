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

import de.bixilon.minosoft.game.datatypes.world.BlockPosition;
import de.bixilon.minosoft.game.datatypes.TextComponent;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OutByteBuffer {
    private final List<Byte> bytes = new ArrayList<>();

    public OutByteBuffer() {
    }

    public void writeByte(byte b) {
        bytes.add(b);
    }

    public void writeByte(byte b, List<Byte> write) {
        write.add(b);
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

    public void writeInteger(int i) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(i);
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
        if (s.length() > ProtocolDefinition.STRING_MAX_LEN) {
            writeByte((byte) 0); // write length 0
        }
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

    public void writeVarInt(int value, List<Byte> write) {
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
        writeInteger((int) (d * 32.0D));
    }

    public void writeFixedPointNumberByte(double d) {
        writeInteger((int) (d * 32.0D));
    }

    public List<Byte> getBytes() {
        return bytes;
    }

    public void writeJson(JSONObject j) {
        writeString(j.toString());
    }

    public void writeBlockPosition(BlockPosition pos) {
        writeLong((((long) pos.getX() & 0x3FFFFFF) << 38) | (((long) pos.getZ() & 0x3FFFFFF) << 12) | ((long) pos.getY() & 0xFFF));
    }

    public void writeChatComponent(TextComponent c) {
        writeJson(c.getRaw());
    }
}
