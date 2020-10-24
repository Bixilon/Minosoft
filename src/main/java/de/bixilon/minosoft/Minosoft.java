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
import de.bixilon.minosoft.data.locale.LocaleManager;
import de.bixilon.minosoft.data.locale.minecraft.MinecraftLocaleManager;
import de.bixilon.minosoft.data.mappings.versions.Versions;
import de.bixilon.minosoft.gui.main.*;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.logging.LogLevels;
import de.bixilon.minosoft.modding.event.EventManager;
import de.bixilon.minosoft.modding.loading.ModLoader;
import de.bixilon.minosoft.modding.loading.Priorities;
import de.bixilon.minosoft.util.CountUpAndDownLatch;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.mojang.api.MojangAccount;
import de.bixilon.minosoft.util.task.AsyncTaskWorker;
import de.bixilon.minosoft.util.task.Task;
import de.bixilon.minosoft.util.task.TaskImportance;
import javafx.application.Platform;
import javafx.scene.control.Dialog;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.*;

public final class Minosoft {
    public static final HashSet<EventManager> eventManagers = new HashSet<>();
    private static final CountUpAndDownLatch startStatusLatch = new CountUpAndDownLatch();
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
        AsyncTaskWorker taskWorker = new AsyncTaskWorker();

        taskWorker.setFatalError((exception) -> {
            Log.fatal("Critical error occurred while preparing. Exit");
            if (StartProgressWindow.toolkitLatch.getCount() == 2) {
                try {
                    StartProgressWindow.start();
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                    System.exit(1);
                }
            }
            if (StartProgressWindow.toolkitLatch.getCount() > 0) {
                try {
                    StartProgressWindow.toolkitLatch.await();
                } catch (InterruptedException e2) {
                    e2.printStackTrace();
                    System.exit(1);
                }
            }
            // hide all other gui parts
            StartProgressWindow.hideDialog();
            Launcher.exit();
            Platform.runLater(() -> {
                Dialog<Boolean> dialog = new Dialog<>();
                dialog.setTitle("Critical Error");
                dialog.setHeaderText("An error occurred while starting Minosoft");
                dialog.setContentText(exception.getLocalizedMessage());

                Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                stage.setAlwaysOnTop(true);
                stage.toFront();
                dialog.setOnCloseRequest(dialogEvent -> System.exit(1));
                dialog.showAndWait();
                System.exit(1);
            });
        });

        taskWorker.addTask(new Task((progress) -> StartProgressWindow.start(), "JavaFx Toolkit", "", Priorities.HIGHEST));

        taskWorker.addTask(new Task((progress) -> StartProgressWindow.show(startStatusLatch), "Progress Window", "", Priorities.HIGH, TaskImportance.OPTIONAL, new HashSet<>(Collections.singleton("JavaFx Toolkit"))));

        taskWorker.addTask(new Task(progress -> {
            progress.countUp();
            LocaleManager.load(config.getString(ConfigurationPaths.GENERAL_LANGUAGE));
            progress.countDown();

        }, "Minosoft Language", "", Priorities.HIGH, TaskImportance.REQUIRED));

        taskWorker.addTask(new Task(progress -> {
            progress.countUp();
            Log.info("Loading versions.json...");
            long mappingStartLoadingTime = System.currentTimeMillis();
            Versions.load(Util.readJsonAsset("mapping/versions.json"));
            Log.info(String.format("Loaded versions mapping in %dms", (System.currentTimeMillis() - mappingStartLoadingTime)));
            progress.countDown();

        }, "Version mappings", "", Priorities.NORMAL, TaskImportance.REQUIRED));

        taskWorker.addTask(new Task(progress -> {
            Log.debug("Refreshing account token...");
            checkClientToken();
            accountList = config.getMojangAccounts();
            selectAccount(accountList.get(config.getString(ConfigurationPaths.ACCOUNT_SELECTED)));

        }, "Token refresh", "", Priorities.LOW));

        taskWorker.addTask(new Task(progress -> {
            progress.countUp();
            ModLoader.loadMods(progress);
            progress.countDown();

        }, "ModLoading", "", Priorities.NORMAL, TaskImportance.REQUIRED));

        taskWorker.addTask(new Task(progress -> Launcher.start(), "Launcher", "", Priorities.HIGH, TaskImportance.OPTIONAL, new HashSet<>(Arrays.asList("Minosoft Language", "JavaFx Toolkit"))));

        taskWorker.addTask(new Task(progress -> {
            progress.countUp();
            AssetsManager.downloadAllAssets(progress);
            progress.countDown();

        }, "Assets", "", Priorities.HIGH, TaskImportance.REQUIRED));


        taskWorker.addTask(new Task(progress -> {
            progress.countUp();
            MinecraftLocaleManager.load(config.getString(ConfigurationPaths.GENERAL_LANGUAGE));
            progress.countDown();

        }, "Mojang language", "", Priorities.HIGH, TaskImportance.REQUIRED, new HashSet<>(Collections.singleton("Assets"))));


        taskWorker.work(startStatusLatch);
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
            startStatusLatch.waitUntilZero();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static CountUpAndDownLatch getStartStatusLatch() {
        return startStatusLatch;
    }
}
