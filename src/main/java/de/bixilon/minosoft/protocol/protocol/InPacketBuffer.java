package de.bixilon.minosoft.protocol.protocol;

public class InPacketBuffer extends InByteBuffer {
    private final int command;
    private final int length; // not interested in yet

    public InPacketBuffer(byte[] bytes) {
        super(bytes);
        // ToDo: compression
        length = readVarInt();
        command = readVarInt();
    }

    public int getCommand() {
        return command;
    }
}
