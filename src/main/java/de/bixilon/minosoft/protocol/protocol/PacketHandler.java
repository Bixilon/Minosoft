package de.bixilon.minosoft.protocol.protocol;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.login.PacketEncryptionKeyRequest;
import de.bixilon.minosoft.protocol.packets.clientbound.login.PacketLoginDisconnect;
import de.bixilon.minosoft.protocol.packets.clientbound.login.PacketLoginSuccess;
import de.bixilon.minosoft.protocol.packets.clientbound.play.*;
import de.bixilon.minosoft.protocol.packets.clientbound.status.PacketStatusPong;
import de.bixilon.minosoft.protocol.packets.clientbound.status.PacketStatusResponse;
import de.bixilon.minosoft.protocol.packets.serverbound.login.PacketEncryptionResponse;
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketKeepAliveResponse;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;

public class PacketHandler {
    Connection connection;

    public PacketHandler(Connection connection) {
        this.connection = connection;
    }

    public void handle(PacketStatusResponse pkg) {
        Log.info(String.format("Status response received: %s/%s online. MotD: '%s'", pkg.getResponse().getPlayerOnline(), pkg.getResponse().getMaxPlayers(), pkg.getResponse().getMotd()));
    }

    public void handle(PacketStatusPong pkg) {
        Log.debug("Pong: " + pkg.getID());
        if (connection.isOnlyPing()) {
            // pong arrived, closing connection
            connection.disconnect();
        }
    }

    public void handle(PacketEncryptionKeyRequest pkg) {
        SecretKey secretKey = CryptManager.createNewSharedKey();
        PublicKey publicKey = CryptManager.decodePublicKey(pkg.getPublicKey());
        String serverHash = new BigInteger(CryptManager.getServerHash(pkg.getServerId(), publicKey, secretKey)).toString(16);
        connection.getPlayer().getAccount().join(serverHash);
        connection.sendPacket(new PacketEncryptionResponse(secretKey, pkg.getVerifyToken(), publicKey));

    }

    public void handle(PacketLoginSuccess pkg) {
        // now we are playing
        connection.setConnectionState(ConnectionState.PLAY);
    }

    public void handle(PacketJoinGame pkg) {
    }

    public void handle(PacketLoginDisconnect pkg) {
        Log.info(String.format("Disconnecting from server(%s)", pkg.getReason().toString()));
        connection.setConnectionState(ConnectionState.DISCONNECTING);
    }

    public void handle(PacketPlayerInfo pkg) {
    }

    public void handle(PacketTimeUpdate pkg) {
    }

    public void handle(PacketKeepAlive pkg) {
        connection.sendPacket(new PacketKeepAliveResponse(pkg.getId()));
    }

    public void handle(PlayChunkBulk pkg) {
        //ToDo
    }
}
