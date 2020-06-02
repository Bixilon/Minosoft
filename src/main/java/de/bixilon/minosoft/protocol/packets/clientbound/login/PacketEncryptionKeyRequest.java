package de.bixilon.minosoft.protocol.packets.clientbound.login;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ClientboundPacket;
import de.bixilon.minosoft.protocol.protocol.InPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.PacketHandler;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class PacketEncryptionKeyRequest implements ClientboundPacket {
    String serverId; //normally empty
    byte[] publicKey;
    byte[] verifyToken;

    @Override
    public void read(InPacketBuffer buffer, ProtocolVersion v) {
        switch (v) {
            case VERSION_1_7_10:
                serverId = buffer.readString();
                publicKey = buffer.readBytes(buffer.readShort()); // read length, then the bytes
                verifyToken = buffer.readBytes(buffer.readShort()); // read length, then the bytes
                break;
        }  // ToDo

        log();
    }

    @Override
    public void log() {
        Log.protocol("Receiving encryption request packet");
    }

    @Override
    public void handle(PacketHandler h) {
        h.handle(this);
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public byte[] getVerifyToken() {
        return verifyToken;
    }

    public String getServerId() {
        return serverId;
    }
}
