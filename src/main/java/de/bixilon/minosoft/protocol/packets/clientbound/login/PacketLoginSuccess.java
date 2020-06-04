package de.bixilon.minosoft.protocol.packets.clientbound.login;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import java.util.UUID;

public class PacketLoginSuccess implements ClientboundPacket {
    UUID uuid;
    String username;

    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        uuid = UUID.fromString(buffer.readString());
        username = buffer.readString();

        log();
    }

    @Override
    public void log() {
        Log.protocol(String.format("Receiving login success packet (username: %s, UUID: %s)", username, uuid.toString()));
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getUsername() {
        return username;
    }
}
