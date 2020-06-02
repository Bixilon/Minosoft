package de.bixilon.minosoft.protocol.protocol;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class InByteBuffer {
    private final byte[] bytes;
    private int pos;

    public InByteBuffer(byte[] bytes) {
        this.bytes = bytes;
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

    public boolean readBoolean() {
        boolean ret;
        ret = readByte() == 1;
        return ret;
    }

    public short readShort() {
        ByteBuffer buffer = ByteBuffer.allocate(Short.BYTES);
        buffer.put(readBytes(Short.BYTES));
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer.getShort();
    }

    public int readInteger() {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(readBytes(Integer.BYTES));
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer.getInt();
    }

    public Long readLong() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(readBytes(Long.BYTES));
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer.getLong();
    }

    public Float readFloat() {
        ByteBuffer buffer = ByteBuffer.allocate(Float.BYTES);
        buffer.put(readBytes(Float.BYTES));
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer.getFloat();
    }

    public Double readDouble() {
        ByteBuffer buffer = ByteBuffer.allocate(Double.BYTES);
        buffer.put(readBytes(Double.BYTES));
        buffer.order(ByteOrder.BIG_ENDIAN);
        return buffer.getDouble();
    }

    public String readString() {
        int length = readVarInt();
        if (length > ProtocolDefinition.STRING_MAX_LEN) {
            // ToDo throw new PacketDataException(String.format("String is longer than %s", ProtocolDefinition.STRING_MAX_LEN));
            return null;
        }
        return new String(readBytes(length), StandardCharsets.UTF_8);
    }

    public UUID readUUID() {
        ByteBuffer buffer = ByteBuffer.allocate(16); // UUID.BYTES
        buffer.put(readBytes(16));
        buffer.order(ByteOrder.BIG_ENDIAN);
        return new UUID(buffer.getLong(0), buffer.getLong(1));
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


}
