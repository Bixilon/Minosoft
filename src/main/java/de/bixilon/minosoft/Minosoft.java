/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft;

import de.bixilon.minosoft.config.Configuration;
import de.bixilon.minosoft.config.GameConfiguration;
import de.bixilon.minosoft.game.datatypes.objectLoader.versions.Versions;
import de.bixilon.minosoft.gui.main.Server;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.logging.LogLevels;
import de.bixilon.minosoft.util.FolderUtil;
import de.bixilon.minosoft.util.OSUtil;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.mojang.api.MojangAccount;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class Minosoft {
    static Configuration config;
    public static ArrayList<MojangAccount> accountList;
    public static ArrayList<Server> serverList;

    public static void main(String[] args) {
        // init log thread
        Log.initThread();

        Log.info("Starting...");
        setConfigFolder();
        Log.info("Reading config file...");
        try {
            config = new Configuration(Config.configFileName);
        } catch (IOException e) {
            Log.fatal("Failed to load config file!");
            e.printStackTrace();
            return;
        }
        Log.info(String.format("Loaded config file (version=%s)", config.getInt(GameConfiguration.CONFIG_VERSION)));
        // set log level from config
        Log.setLevel(LogLevels.valueOf(config.getString(GameConfiguration.GENERAL_LOG_LEVEL)));
        Log.info(String.format("Logging info with level: %s", Log.getLevel()));
        Log.info("Checking assets...");
        checkAssets();
        Log.info("Assets checking done");
        Log.info("Loading versions.json...");
        long mappingStartLoadingTime = System.currentTimeMillis();
        try {
            Versions.load(Util.readJsonFromFile(Config.homeDir + "assets/mapping/versions.json"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        Log.info(String.format("Loaded versions mapping in %dms", (System.currentTimeMillis() - mappingStartLoadingTime)));

        checkClientToken();

        accountList = config.getMojangAccounts();
        if (accountList.size() == 0) {
            /*
            MojangAccount account = MojangAuthentication.login("email", "password");
            account.saveToConfig();
             */
            throw new RuntimeException("No accounts in config file!");
        }
        MojangAccount account = accountList.get(0);
        if (account.refreshToken()) {
            // could not login
            account.saveToConfig();
        } else {
            Log.mojang("Could not refresh session, you will not be able to join premium servers!");
        }

        serverList = config.getServers();
        Launcher.main();
    }

    /**
     * Sets Config.homeDir to the correct folder per OS
     */
    public static void setConfigFolder() {
        String path = System.getProperty("user.home");
        if (!path.endsWith(File.separator)) {
            path += "/";
        }
        switch (OSUtil.getOS()) {
            case LINUX -> path += ".local/share/minosoft/";
            case WINDOWS -> path += "AppData/Roaming/Minosoft/";
            case MAC -> path += "Library/Application Support/Minosoft/";
            case OTHER -> path += ".minosoft/";
        }
        File folder = new File(path);
        if (!folder.exists() && !folder.mkdirs()) {
            // failed creating folder
            throw new RuntimeException(String.format("Could not create home folder (%s)!", path));
        }
        Config.homeDir = path;
    }

    public static Configuration getConfig() {
        return config;
    }

    public static void checkClientToken() {
        if (config.getString(GameConfiguration.CLIENT_TOKEN) == null || config.getString(GameConfiguration.CLIENT_TOKEN).equals("randomGenerated")) {
            config.putString(GameConfiguration.CLIENT_TOKEN, UUID.randomUUID().toString());
            config.saveToFile(Config.configFileName);
        }
    }

    private static void checkAssets() {
        try {
            FolderUtil.copyFolder(Minosoft.class.getResource("/assets").toURI(), Config.homeDir + "assets/");
        } catch (Exception e) {
            Log.fatal("Error occurred while checking assets: " + e.getLocalizedMessage());
            System.exit(1);
        }
    }
}
