package de.bixilon.minosoft.protocol.packets.clientbound.login;

import de.bixilon.minosoft.game.datatypes.ChatComponent;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketLoginDisconnect implements ClientboundPacket {
    ChatComponent reason;

    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        reason = buffer.readChatComponent();
        log();
    }

    @Override
    public void log() {
        Log.protocol(String.format("Receiving login disconnect packet (%s)", reason.getRawMessage()));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public ChatComponent getReason() {
        return reason;
    }
}
