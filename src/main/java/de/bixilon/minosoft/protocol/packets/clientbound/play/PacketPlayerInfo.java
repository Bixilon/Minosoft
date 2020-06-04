package de.bixilon.minosoft.protocol.packets.clientbound.play;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketPlayerInfo implements ClientboundPacket {
    String name;
    PlayerJoinState state;
    short ping;


    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                name = buffer.readString();
                state = (buffer.readBoolean() ? PlayerJoinState.JOINED : PlayerJoinState.DISCONNECTED);
                ping = buffer.readShort();

                break;
        }
        log();
    }

    @Override
    public void log() {
        Log.game(String.format("[TAB] %s %s", name, (state == PlayerJoinState.JOINED ? "added" : "removed")));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public enum PlayerJoinState {
        JOINED,
        DISCONNECTED
    }
}
