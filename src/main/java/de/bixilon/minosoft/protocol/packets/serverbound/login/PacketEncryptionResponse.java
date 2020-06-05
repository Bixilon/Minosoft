package de.bixilon.minosoft.protocol.packets.serverbound.login;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.packets.ServerboundPacket;
import de.bixilon.minosoft.protocol.protocol.CryptManager;
import de.bixilon.minosoft.protocol.protocol.OutPacketBuffer;
import de.bixilon.minosoft.protocol.protocol.Packets;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

import javax.crypto.SecretKey;
import java.security.PublicKey;

public class PacketEncryptionResponse implements ServerboundPacket {

    final byte[] secret;
    final byte[] token;
    final SecretKey secretKey;


    public PacketEncryptionResponse(SecretKey secret, byte[] token, PublicKey key) {
        this.secretKey = secret;
        this.secret = CryptManager.encryptData(key, secret.getEncoded());
        this.token = CryptManager.encryptData(key, token);
    }


    public SecretKey getSecretKey() {
        return secretKey;
    }

    @Override
    public OutPacketBuffer write(ProtocolVersion v) {
        log();
        OutPacketBuffer buffer = new OutPacketBuffer(v.getPacketCommand(Packets.Serverbound.LOGIN_ENCRYPTION_RESPONSE));
        switch (v) {
            case VERSION_1_7_10:
                buffer.writeShort((short) secret.length);
                buffer.writeBytes(secret);
                buffer.writeShort((short) token.length);
                buffer.writeBytes(token);
        }
        //buffer.writeString(username);
        return buffer;
    }

    @Override
    public void log() {
        Log.protocol("Sending encryption response");
    }
}
