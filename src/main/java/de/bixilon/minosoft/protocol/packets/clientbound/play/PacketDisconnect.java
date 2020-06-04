package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.ChatComponent;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketDisconnect implements ClientboundPacket {
    ChatComponent reason;


    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                reason = buffer.readChatComponent();
                break;
        }
        log();
    }

    @Override
    public void log() {
        Log.game(String.format("Disconnected: %s", reason.getRawMessage()));
    }

    public ChatComponent getReason() {
        return reason;
    }


    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }
}
