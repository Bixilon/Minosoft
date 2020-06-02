package de.bixilon.minosoft;

import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;

public class Minosoft {
    public static void main(String[] args) {
        Log.info("Starting...");
        Connection c = new Connection("127.0.0.1", 25565);
        c.connect();
    }
}
