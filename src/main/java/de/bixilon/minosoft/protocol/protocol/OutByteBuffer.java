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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.bixilon.minosoft.data.inventory.Slot;
import de.bixilon.minosoft.data.text.ChatComponent;
import de.bixilon.minosoft.data.world.BlockPosition;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.util.nbt.tag.CompoundTag;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.UUID;

import static de.bixilon.minosoft.protocol.protocol.Versions.*;

public class OutByteBuffer {
    final ArrayList<Byte> bytes;
    final Connection connection;
    final int versionId;

    public OutByteBuffer(Connection connection) {
        this.bytes = new ArrayList<>();
        this.connection = connection;
        this.versionId = connection.getVersion().getVersionId();
    }

    @SuppressWarnings("unchecked")
    public OutByteBuffer(OutByteBuffer buffer) {
        this.bytes = (ArrayList<Byte>) buffer.getBytes().clone();
        this.connection = buffer.getConnection();
        this.versionId = buffer.getVersionId();
    }

    public void writeByteArray(byte[] data) {
        if (this.versionId < V_14W21A) {
            writeShort((short) data.length);
        } else {
            writeVarInt(data.length);
        }
        writeBytes(data);
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
        writeString(new Gson().toJson(json));
    }

    public void writeString(String string) {
        if (string.length() > ProtocolDefinition.STRING_MAX_LEN) {
            throw new IllegalArgumentException(String.format("String max string length exceeded %d > %d", string.length(), ProtocolDefinition.STRING_MAX_LEN));
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

    public void writePosition(BlockPosition position) {
        if (position == null) {
            writeLong(0L);
            return;
        }
        if (this.versionId < V_18W43A) {
            writeLong((((long) position.getX() & 0x3FFFFFF) << 38) | (((long) position.getZ() & 0x3FFFFFF)) | ((long) position.getY() & 0xFFF) << 26);
            return;
        }
        writeLong((((long) (position.getX() & 0x3FFFFFF) << 38) | ((long) (position.getZ() & 0x3FFFFFF) << 12) | (long) (position.getY() & 0xFFF)));
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

    public void writeSlot(Slot slot) {
        if (this.versionId < V_1_13_2_PRE1) {
            if (slot == null) {
                writeShort((short) -1);
                return;
            }
            writeShort((short) (int) this.connection.getMapping().getItemId(slot.getItem()));
            writeByte((byte) slot.getItemCount());
            writeShort(slot.getItemMetadata());
            writeNBT(slot.getNbt(this.connection.getMapping()));
        }
        if (slot == null) {
            writeBoolean(false);
            return;
        }
        writeVarInt(this.connection.getMapping().getItemId(slot.getItem()));
        writeByte((byte) slot.getItemCount());
        writeNBT(slot.getNbt(this.connection.getMapping()));
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

    public void writeBlockPositionByte(BlockPosition pos) {
        writeInt(pos.getX());
        writeByte((byte) pos.getY());
        writeInt(pos.getZ());
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

    public int getVersionId() {
        return this.versionId;
    }

    public void writeEntityId(int entityId) {
        if (this.versionId < V_14W04A) {
            writeInt(entityId);
        } else {
            writeVarInt(entityId);
        }
    }

    public Connection getConnection() {
        return this.connection;
    }
}
