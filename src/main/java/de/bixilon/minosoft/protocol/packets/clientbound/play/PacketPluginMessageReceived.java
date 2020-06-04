package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketPluginMessageReceived implements ClientboundPacket {
    String channel;
    byte[] data;


    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                channel = buffer.readString();
                data = buffer.readBytes(buffer.readShort()); // first read length, then the data
                break;
        }
        log();
    }

    @Override
    public void log() {
        Log.protocol(String.format("Plugin message received in channel %s with %s bytes of data", channel, data.length));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }
}
