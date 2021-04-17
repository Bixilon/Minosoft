/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
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

import com.google.gson.JsonObject;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.protocol.network.connection.Connection;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;
import glm_.vec3.Vec3i;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

public class OutByteBuffer {
    private final ArrayList<Byte> bytes;
    private final Connection connection;

    public OutByteBuffer() {
        this.bytes = new ArrayList<>();
        this.connection = null;
    }

    public OutByteBuffer(Connection connection) {
        this.bytes = new ArrayList<>();
        this.connection = connection;
    }

    @SuppressWarnings("unchecked")
    public OutByteBuffer(OutByteBuffer buffer) {
        this.bytes = (ArrayList<Byte>) buffer.getBytes().clone();
        this.connection = buffer.getConnection();
    }

    public void writeShort(short value) {
        writeByte(value >>> 8);
        writeByte(value);
    }

    public void writeInt(int value) {
        writeByte(value >>> 24);
        writeByte(value >>> 16);
        writeByte(value >>> 8);
        writeByte(value);
    }

    public void writeBytes(byte[] data) {
        for (byte singleByte : data) {
            this.bytes.add(singleByte);
        }
    }

    public void writeLong(long value) {
        writeInt((int) (value >> 32));
        writeInt((int) (value));
    }

    public void writeChatComponent(ChatComponent chatComponent) {
        writeString(chatComponent.getLegacyText()); // ToDo: test if this should not be json
    }

    public void writeJSON(JsonObject json) {
        writeString(Util.GSON.toJson(json));
    }

    public void writeString(String string) {
        if (string.length() > ProtocolDefinition.STRING_MAX_LENGTH) {
            throw new IllegalArgumentException(String.format("String max string length exceeded %d > %d", string.length(), ProtocolDefinition.STRING_MAX_LENGTH));
        }
        writeVarInt(string.length());
        writeBytes(string.getBytes(StandardCharsets.UTF_8));
    }

    public void writeVarLong(long value) {
        do {
            byte temp = (byte) (value & 0x7F);
            value >>>= 7;
            if (value != 0) {
                temp |= 0x80;
            }
            writeByte(temp);
        } while (value != 0);
    }

    public void writeByte(byte value) {
        this.bytes.add(value);
    }

    public void writeByte(int value) {
        this.bytes.add((byte) (value & 0xFF));
    }

    public void writeByte(long value) {
        this.bytes.add((byte) (value & 0xFF));
    }

    public void writeFloat(float value) {
        writeInt(Float.floatToIntBits(value));
    }

    public void writeDouble(double value) {
        writeLong(Double.doubleToLongBits(value));
    }

    public void writeUUID(UUID uuid) {
        writeLong(uuid.getMostSignificantBits());
        writeLong(uuid.getLeastSignificantBits());
    }

    public void writeFixedPointNumberInt(double d) {
        writeInt((int) (d * 32.0D));
    }

    public ArrayList<Byte> getBytes() {
        return this.bytes;
    }

    public void writeVarInt(int value) {
        // thanks https://wiki.vg/Protocol#VarInt_and_VarLong
        do {
            byte temp = (byte) (value & 0x7F);
            value >>>= 7;
            if (value != 0) {
                temp |= 0x80;
            }
            writeByte(temp);
        } while (value != 0);
    }

    public void prefixVarInt(int value) {
        int count = 0;
        // thanks https://wiki.vg/Protocol#VarInt_and_VarLong
        do {
            byte temp = (byte) (value & 0x7F);
            value >>>= 7;
            if (value != 0) {
                temp |= 0x80;
            }
            this.bytes.add(count++, temp);
        } while (value != 0);
    }

    void writeNBT(CompoundTag nbt) {
        // ToDo: test
        nbt.writeBytes(this);
    }

    public void writeBoolean(boolean value) {
        this.bytes.add((byte) ((value) ? 0x01 : 0x00));
    }

    public void writeStringNoLength(String string) {
        writeBytes(string.getBytes(StandardCharsets.UTF_8));
    }

    public void writeVec3iByte(Vec3i position) {
        writeInt(position.getX());
        writeByte((byte) (int) position.getY());
        writeInt(position.getZ());
    }

    public byte[] toByteArray() {
        byte[] ret = new byte[this.bytes.size()];
        for (int i = 0; i < this.bytes.size(); i++) {
            ret[i] = this.bytes.get(i);
        }
        return ret;
    }

    public void writeInts(int[] data) {
        for (int i : data) {
            writeInt(i);
        }
    }

    public void writeLongs(long[] data) {
        for (long l : data) {
            writeLong(l);
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void writeTo(ByteBuffer buffer) {
        for (byte b : this.bytes) {
            buffer.put(b);
        }
    }
}
