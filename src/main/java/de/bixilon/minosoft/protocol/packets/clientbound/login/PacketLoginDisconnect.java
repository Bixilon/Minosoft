package de.bixilon.minosoft.protocol.packets.clientbound.login;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;
import org.json.JSONObject;

public class PacketLoginDisconnect implements ClientboundPacket {
    JSONObject reason;

    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        reason = buffer.readJson();
        log();
    }

    @Override
    public void log() {
        Log.protocol("Receiving login disconnect packet");
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public JSONObject getReason() {
        return reason;
    }
}
