package de.bixilon.minosoft.protocol.packets.serverbound.login;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.objects.Player;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketLoginStart implements ServerboundPacket {

    private final String username;

    public PacketLoginStart(Player p) {
        username = p.getPlayerName();
        log();
    }

    public PacketLoginStart(String username) {
        this.username = username;
        log();
    }

    @Override
    public OutPacketBuffer write(ProtocolVersion v) {
        // no version checking, is the same in all versions (1.7.x - 1.15.2)
        OutPacketBuffer buffer = new OutPacketBuffer(v.getPacketCommand(Packets.Serverbound.LOGIN_LOGIN_START));
        buffer.writeString((username == null) ? "Player132" : username);
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol(String.format("Sending login start (%s)", username));
    }
}
