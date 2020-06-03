package de.bixilon.minosoft.protocol.packets.serverbound.play;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketKeepAliveResponse implements ServerboundPacket {

    private final int id;

    public PacketKeepAliveResponse(int id) {
        this.id = id;
    }


    @Override
    public OutPacketBuffer write(ProtocolVersion v) {
        log();
        OutPacketBuffer buffer = new OutPacketBuffer(v.getPacketCommand(Packets.Serverbound.LOGIN_LOGIN_START));
        switch (v) {
            case VERSION_1_7_10:
                buffer.writeInteger(id);
                break;
        }
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Sending keep alive back (%s)", id));
    }
}
