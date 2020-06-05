package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.game.datatypes.ChatComponent;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketChatMessage implements ClientboundPacket {
    ChatComponent c;


    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                c = buffer.readChatComponent();
                break;
        }
    }

    @Override
    public void log() {
        Log.game(String.format("[CHAT] %s", c.getRawMessage()));
    }

    public ChatComponent getChatComponent() {
        return c;
    }


    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }
}
