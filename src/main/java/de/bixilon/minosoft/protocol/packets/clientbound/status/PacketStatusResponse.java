package de.bixilon.minosoft.protocol.packets.clientbound.status;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.objects.ServerListPing;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketStatusResponse implements ClientboundPacket {
    ServerListPing response;

    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        // no version checking, is the same in all versions (1.7.x - 1.15.2)
        response = new ServerListPing(buffer.readJson());
        log();
    }

    @Override
    public void log() {
        Log.protocol(String.format("Receiving status response packet (%s)", response.getRaw().toString()));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public ServerListPing getResponse() {
        return this.response;
    }
}
