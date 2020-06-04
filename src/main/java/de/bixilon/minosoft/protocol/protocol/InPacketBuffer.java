package de.bixilon.minosoft.protocol.protocol;

public class InPacketBuffer extends InByteBuffer {
    private final int command;

    public InPacketBuffer(byte[] bytes) {
        super(bytes);
        // ToDo: compression
        command = readVarInt();
    }

    public int getCommand() {
        return command;
    }
}
