package de.bixilon.minosoft.protocol.protocol;

import java.util.ArrayList;
import java.util.List;

public class OutPacketBuffer extends OutByteBuffer {
    private final int command;

    public OutPacketBuffer(int command) {
        super();
        this.command = command;
    }

    public int getCommand() {
        return command;
    }

    public byte[] getOutBytes() {
        // ToDo: compression
        List<Byte> before = getBytes();
        List<Byte> after = new ArrayList<>();
        List<Byte> last = new ArrayList<>();
        writeVarInt(getCommand(), after); // second: command
        after.addAll(before); // rest ist raw data

        writeVarInt(after.size(), last); // first var int: length
        last.addAll(after); // rest ist raw data

        byte[] ret = new byte[last.size()];
        for (int i = 0; i < last.size(); i++) {
            ret[i] = last.get(i);
        }
        return ret;
    }

}
