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
import de.bixilon.minosoft.config.ConfigurationPaths;
import de.bixilon.minosoft.data.assets.AssetsManager;
import de.bixilon.minosoft.data.mappings.versions.Versions;
import de.bixilon.minosoft.gui.LocaleManager;
import de.bixilon.minosoft.gui.main.AccountListCell;
import de.bixilon.minosoft.gui.main.Launcher;
import de.bixilon.minosoft.gui.main.MainWindow;
import de.bixilon.minosoft.gui.main.Server;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.logging.LogLevels;
import de.bixilon.minosoft.modding.event.EventManager;
import de.bixilon.minosoft.modding.loading.ModLoader;
import de.bixilon.minosoft.util.CountUpAndDownLatch;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.mojang.api.MojangAccount;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

public final class Minosoft {
    public static final HashSet<EventManager> eventManagers = new HashSet<>();
    public static final CountUpAndDownLatch assetsLatch = new CountUpAndDownLatch(1);  // count of files still to download, will be used to show progress
    private static final CountDownLatch startStatusLatch = new CountDownLatch(4); // number of critical components (wait for them before other "big" actions)
    public static HashBiMap<String, MojangAccount> accountList;
    public static MojangAccount selectedAccount;
    public static ArrayList<Server> serverList;
    public static Configuration config;

    public static void main(String[] args) {
        // init log thread
        Log.initThread();

        Log.info("Starting...");
        Log.info("Reading config file...");
        try {
            config = new Configuration(Config.configFileName);
        } catch (IOException e) {
            Log.fatal("Failed to load config file!");
            e.printStackTrace();
            return;
        }
        Log.info(String.format("Loaded config file (version=%s)", config.getInt(ConfigurationPaths.CONFIG_VERSION)));
        // set log level from config
        Log.setLevel(LogLevels.valueOf(config.getString(ConfigurationPaths.GENERAL_LOG_LEVEL)));
        Log.info(String.format("Logging info with level: %s", Log.getLevel()));

        serverList = config.getServers();
        ArrayList<Callable<Boolean>> startCallables = new ArrayList<>();
        startCallables.add(() -> {
            LocaleManager.load(config.getString(ConfigurationPaths.LANGUAGE));
            countDownStart();
            return true;
        });
        startCallables.add(() -> {
            Log.info("Loading versions.json...");
            long mappingStartLoadingTime = System.currentTimeMillis();
            try {
                Versions.load(Util.readJsonAsset("mapping/versions.json"));
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            }
            Log.info(String.format("Loaded versions mapping in %dms", (System.currentTimeMillis() - mappingStartLoadingTime)));
            countDownStart(); // (another) critical component was loaded
            return true;
        });
        startCallables.add(() -> {
            Log.debug("Refreshing client token...");
            checkClientToken();
            accountList = config.getMojangAccounts();
            selectAccount(accountList.get(config.getString(ConfigurationPaths.ACCOUNT_SELECTED)));
            return true;
        });
        startCallables.add(() -> {
            ModLoader.loadMods();
            countDownStart();
            return true;
        });
        startCallables.add(() -> {
            Launcher.start();
            return true;
        });
        startCallables.add(() -> {
            AssetsManager.downloadAllAssets(assetsLatch);
            countDownStart();
            return true;
        });
        // If you add another "critical" component (wait for them at startup): You MUST adjust increment the number of the counter in `startStatus` (See in the first lines of this file)
        try {
            Util.executeInThreadPool("Start", startCallables);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void countDownStart() {
        startStatusLatch.countDown();
        Launcher.setProgressBar((int) startStatusLatch.getCount());
    }

    public static void checkClientToken() {
        if (config.getString(ConfigurationPaths.CLIENT_TOKEN).isBlank()) {
            config.putString(ConfigurationPaths.CLIENT_TOKEN, UUID.randomUUID().toString());
            config.saveToFile();
        }
    }

    public static void selectAccount(MojangAccount account) {
        if (account == null) {
            selectedAccount = null;
            config.putString(ConfigurationPaths.ACCOUNT_SELECTED, "");
            config.saveToFile();
            return;
        }
        MojangAccount.RefreshStates refreshState = account.refreshToken();
        if (refreshState == MojangAccount.RefreshStates.ERROR) {
            accountList.remove(account.getUserId());
            account.delete();
            AccountListCell.listView.getItems().remove(account);
            selectedAccount = null;
            return;
        }
        config.putString(ConfigurationPaths.ACCOUNT_SELECTED, account.getUserId());
        selectedAccount = account;
        MainWindow.selectAccount();
        account.saveToConfig();
    }

    public static Configuration getConfig() {
        return config;
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

    /**
     * Waits until all critical components are started
     */
    public static void waitForStartup() {
        try {
            startStatusLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static int getStartUpJobsLeft() {
        return (int) startStatusLatch.getCount();
    }
}
