package de.bixilon.minosoft;

import de.bixilon.minosoft.config.Configuration;
import de.bixilon.minosoft.config.GameConfiguration;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.logging.LogLevel;
import de.bixilon.minosoft.objects.Account;
import de.bixilon.minosoft.objects.Player;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.util.OSUtil;

import java.io.File;
import java.io.IOException;

public class Minosoft {
    static Configuration config;

    public static void main(String[] args) {
        Log.info("Starting...");
        setConfigFolder();
        Log.info("Reading config file...");
        try {
            config = new Configuration("game.yml");
        } catch (IOException e) {
            Log.fatal("Failed to load config file!");
            e.printStackTrace();
            return;
        }
        Log.info(String.format("Loaded config file (version=%s)", config.getInteger(GameConfiguration.CONFIG_VERSION)));
        // set log level from config
        Log.setLevel(LogLevel.byName(config.getString(GameConfiguration.GENERAL_LOG_LEVEL)));

        Connection c = new Connection(config.getString("debug.host"), config.getInteger("debug.port"));
        c.setPlayer(new Player(new Account(config.getString("debug.username"), config.getString("debug.password"))));
        c.connect();
    }

    /**
     * Sets Config.homeDir to the correct folder per OS
     */
    public static void setConfigFolder() {
        String folder = System.getProperty("user.home");
        if (!folder.endsWith(File.separator)) {
            folder += "/";
        }
        switch (OSUtil.getOS()) {
            case LINUX:
                folder += ".local/share/minosoft/";
                break;
            case WINDOWS:
                Config.homeDir = "AppData/Roaming/Minosoft/";
                break;
            //ToDo: Mac, Other
        }
        Config.homeDir = folder;
    }

    public static Configuration getConfig() {
        return config;
    }
}
