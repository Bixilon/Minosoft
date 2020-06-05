package de.bixilon.minosoft.protocol.protocol;

import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.config.GameConfiguration;
import de.bixilon.minosoft.game.datatypes.GameMode;
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
import de.bixilon.minosoft.protocol.packets.serverbound.play.PacketPluginMessageSending;

import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.security.PublicKey;

public class PacketHandler {
    final Connection connection;

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
        // already done in packet thread
        // connection.setConnectionState(ConnectionState.PLAY);
    }

    public void handle(PacketJoinGame pkg) {
        connection.getPlayer().setGameMode(pkg.getGameMode());
        connection.getPlayer().setEntityId(pkg.getEntityId());
        connection.getPlayer().getWorld().setHardcore(pkg.isHardcore());
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

    public void handle(PacketChunkBulk pkg) {
        connection.getPlayer().getWorld().setChunks(pkg.getChunkMap());
    }

    public void handle(PacketUpdateHealth pkg) {
        connection.getPlayer().setFood(pkg.getFood());
        connection.getPlayer().setHealth(pkg.getHealth());
        connection.getPlayer().setSaturation(pkg.getSaturation());
    }

    public void handle(PacketPluginMessageReceiving pkg) {
        if (pkg.getChannel().equals("MC|Brand")) {
            // server brand received
            Log.info(String.format("Server is running %s on version %s", new String(pkg.getData()), connection.getVersion().getName()));

            // send back own brand
            connection.sendPacket(new PacketPluginMessageSending("MC|Brand", (Minosoft.getConfig().getBoolean(GameConfiguration.NETWORK_FAKE_CLIENT_BRAND) ? "vanilla" : "Minosoft")));
        }
    }

    public void handle(PacketSpawnLocation pkg) {
        connection.getPlayer().setSpawnLocation(pkg.getSpawnLocation());
    }

    public void handle(PacketChatMessage pkg) {
    }

    public void handle(PacketDisconnect pkg) {
        // got kicked
        connection.setConnectionState(ConnectionState.DISCONNECTING);
    }

    public void handle(PacketHeldItemChangeReceiving pkg) {
        connection.getPlayer().setSelectedSlot(pkg.getSlot());
    }

    public void handle(PacketSetExperience pkg) {
        connection.getPlayer().setLevel(pkg.getLevel());
        connection.getPlayer().setTotalExperience(pkg.getTotal());
    }

    public void handle(PacketChangeGameState pkg) {
        switch (pkg.getReason()) {
            case START_RAIN:
                connection.getPlayer().getWorld().setRaining(true);
                break;
            case END_RAIN:
                connection.getPlayer().getWorld().setRaining(false);
                break;
            case CHANGE_GAMEMODE:
                connection.getPlayer().setGameMode(GameMode.byId(pkg.getValue().intValue()));
                break;
            //ToDo: handle all updates
        }
    }
}
