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

import com.google.common.collect.HashBiMap;
import de.bixilon.minosoft.config.Configuration;
import de.bixilon.minosoft.config.GameConfiguration;
import de.bixilon.minosoft.game.datatypes.objectLoader.versions.Versions;
import de.bixilon.minosoft.gui.main.AccountListCell;
import de.bixilon.minosoft.gui.main.Server;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.logging.LogLevels;
import de.bixilon.minosoft.modding.event.EventManager;
import de.bixilon.minosoft.modding.loading.ModLoader;
import de.bixilon.minosoft.util.OSUtil;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.mojang.api.MojangAccount;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public class Minosoft {
    public static HashBiMap<String, MojangAccount> accountList;
    public static MojangAccount selectedAccount;
    public static ArrayList<Server> serverList;
    public static HashSet<EventManager> globalEventManagers = new HashSet<>();
    static Configuration config;

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
        Log.info("Loading versions.json...");
        long mappingStartLoadingTime = System.currentTimeMillis();
        try {
            Versions.load(Util.readJsonAsset("mapping/versions.json"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        Log.info(String.format("Loaded versions mapping in %dms", (System.currentTimeMillis() - mappingStartLoadingTime)));

        Log.debug("Refreshing client token...");
        checkClientToken();
        accountList = config.getMojangAccounts();
        selectAccount(accountList.get(config.getString(GameConfiguration.ACCOUNT_SELECTED)));

        serverList = config.getServers();
        Thread modThread = new Thread(() -> {
            try {
                ModLoader.loadMods();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        });
        modThread.setName("ModLoader");
        modThread.start();
        Launcher.start();
    }

    /**
     * Sets Config.homeDir to the correct folder per OS
     */
    public static void setConfigFolder() {
        String path = System.getProperty("user.home");
        if (!path.endsWith(File.separator)) {
            path += "/";
        }
        path += switch (OSUtil.getOS()) {
            case LINUX -> ".local/share/minosoft/";
            case WINDOWS -> "AppData/Roaming/Minosoft/";
            case MAC -> "Library/Application Support/Minosoft/";
            case OTHER -> ".minosoft/";
        };
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

    public static ArrayList<Server> getServerList() {
        return serverList;
    }

    public static HashBiMap<String, MojangAccount> getAccountList() {
        return accountList;
    }

    public static MojangAccount getSelectedAccount() {
        return selectedAccount;
    }

    public static void selectAccount(MojangAccount account) {
        if (account == null) {
            selectedAccount = null;
            config.putString(GameConfiguration.ACCOUNT_SELECTED, null);
            config.saveToFile(Config.configFileName);
            return;
        }
        MojangAccount.RefreshStates refreshState = account.refreshToken();
        if (refreshState == MojangAccount.RefreshStates.ERROR) {
            accountList.remove(account.getUserId());
            account.delete();
            if (AccountListCell.listView != null) {
                AccountListCell.listView.getItems().remove(account);
            }
            selectedAccount = null;
            return;
        }
        config.putString(GameConfiguration.ACCOUNT_SELECTED, account.getUserId());
        selectedAccount = account;
        account.saveToConfig();
    }
}
