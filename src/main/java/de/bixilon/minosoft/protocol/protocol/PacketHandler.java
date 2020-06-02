package de.bixilon.minosoft.protocol.protocol;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.packets.clientbound.status.PacketStatusPong;
import de.bixilon.minosoft.protocol.packets.clientbound.status.PacketStatusResponse;

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
}
