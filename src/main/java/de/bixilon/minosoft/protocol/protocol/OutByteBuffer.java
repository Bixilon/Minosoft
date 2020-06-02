package de.bixilon.minosoft.protocol.protocol;

import de.bixilon.minosoft.objects.BlockPosition;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OutByteBuffer {
    private List<Byte> bytes = new ArrayList<>();

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
        buffer.order(ByteOrder.BIG_ENDIAN);
        for (byte b : buffer.array()) {
            bytes.add(b);
        }
    }

    public void writeInteger(int i) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(i);
        buffer.order(ByteOrder.BIG_ENDIAN);
        for (byte b : buffer.array()) {
            bytes.add(b);
        }
    }

    public void writeLong(Long l) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(l);
        buffer.order(ByteOrder.BIG_ENDIAN);
        for (byte b : buffer.array()) {
            bytes.add(b);
        }
    }

    public void writeFloat(Float f) {
        ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES);
        buffer.putFloat(f);
        buffer.order(ByteOrder.BIG_ENDIAN);
        for (byte b : buffer.array()) {
            bytes.add(b);
        }
    }

    public void writeDouble(Double d) {
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
        buffer.putDouble(d);
        buffer.order(ByteOrder.BIG_ENDIAN);
        for (byte b : buffer.array()) {
            bytes.add(b);
        }
    }

    public void writeString(String s) {
        if (s.length() > ProtocolDefinition.STRING_MAX_LEN) {
            //ToDo
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
        buffer.order(ByteOrder.BIG_ENDIAN);
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

    public List<Byte> getBytes() {
        return bytes;
    }

    public void writeJson(JSONObject j) {
        writeString(j.toString());
    }

    public void writeBlockPosition(BlockPosition pos) {
        writeLong((((long) pos.getX() & 0x3FFFFFF) << 38) | (((long) pos.getZ() & 0x3FFFFFF) << 12) | ((long) pos.getY() & 0xFFF));
    }
}
