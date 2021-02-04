/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft;

import com.google.common.collect.HashBiMap;
import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXDialogLayout;
import de.bixilon.minosoft.config.Configuration;
import de.bixilon.minosoft.config.ConfigurationPaths;
import de.bixilon.minosoft.config.StaticConfiguration;
import de.bixilon.minosoft.data.accounts.Account;
import de.bixilon.minosoft.data.assets.Resources;
import de.bixilon.minosoft.data.locale.LocaleManager;
import de.bixilon.minosoft.data.mappings.versions.Versions;
import de.bixilon.minosoft.gui.main.GUITools;
import de.bixilon.minosoft.gui.main.Launcher;
import de.bixilon.minosoft.gui.main.ServerListCell;
import de.bixilon.minosoft.gui.main.StartProgressWindow;
import de.bixilon.minosoft.gui.main.cells.AccountListCell;
import de.bixilon.minosoft.modding.event.EventManager;
import de.bixilon.minosoft.modding.loading.ModLoader;
import de.bixilon.minosoft.modding.loading.Priorities;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.protocol.LANServerListener;
import de.bixilon.minosoft.terminal.CLI;
import de.bixilon.minosoft.util.CountUpAndDownLatch;
import de.bixilon.minosoft.util.MinosoftCommandLineArguments;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.logging.Log;
import de.bixilon.minosoft.util.logging.LogLevels;
import de.bixilon.minosoft.util.task.AsyncTaskWorker;
import de.bixilon.minosoft.util.task.Task;
import de.bixilon.minosoft.util.task.TaskImportance;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.HashSet;
import java.util.UUID;

public final class Minosoft {
    public static final HashSet<EventManager> EVENT_MANAGERS = new HashSet<>();
    public static final HashBiMap<Integer, Connection> CONNECTIONS = HashBiMap.create();
    private static final CountUpAndDownLatch START_STATUS_LATCH = new CountUpAndDownLatch(1);
    private static Configuration config;
    private static boolean isExiting;

    public static void main(String[] args) {
        MinosoftCommandLineArguments.parseCommandLineArguments(args);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(ShutdownReasons.UNKNOWN), "ShutdownHook"));
        Util.initUtilClasses();

        Log.info("Starting...");
        AsyncTaskWorker taskWorker = new AsyncTaskWorker("StartUp");

        taskWorker.setFatalError((exception) -> {
            Log.fatal("Critical error occurred while preparing. Exit");
            if (StaticConfiguration.HEADLESS_MODE) {
                shutdown(exception.getMessage(), ShutdownReasons.CRITICAL_EXCEPTION);
                return;
            }
            try {
                if (StartProgressWindow.TOOLKIT_LATCH.getCount() == 2) {
                    StartProgressWindow.start();
                }
                StartProgressWindow.TOOLKIT_LATCH.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                shutdown(e.getMessage(), ShutdownReasons.CRITICAL_EXCEPTION);
            }
            // hide all other gui parts
            StartProgressWindow.hideDialog();
            Launcher.exit();
            Platform.runLater(() -> {
                JFXAlert<Boolean> dialog = new JFXAlert<>();
                GUITools.initializePane(dialog.getDialogPane());
                // Do not translate this, translations might fail to load...
                dialog.setTitle("Critical Error");
                JFXDialogLayout layout = new JFXDialogLayout();
                layout.setHeading(new Text("A fatal error occurred while starting Minosoft"));
                TextArea text = new TextArea(exception.getClass().getCanonicalName() + ": " + exception.getMessage());
                text.setEditable(false);
                text.setWrapText(true);
                layout.setBody(text);
                dialog.getDialogPane().setContent(layout);

                Stage stage = (Stage) dialog.getDialogPane().getScene().getWindow();
                stage.toFront();
                stage.setOnCloseRequest(dialogEvent -> {
                    dialog.setResult(Boolean.TRUE);
                    dialog.close();
                    shutdown(exception.getMessage(), ShutdownReasons.CRITICAL_EXCEPTION);
                });
                dialog.showAndWait();
                shutdown(exception.getMessage(), ShutdownReasons.CRITICAL_EXCEPTION);
            });
        });
        taskWorker.addTask(new Task(progress -> {
            Log.info("Reading config file...");
            try {
                config = new Configuration();
            } catch (IOException e) {
                Log.fatal("Failed to load config file!");
                e.printStackTrace();
                return;
            }
            Log.info(String.format("Loaded config file (version=%s)", config.getInt(ConfigurationPaths.IntegerPaths.GENERAL_CONFIG_VERSION)));
            // set log level from config
            Log.setLevel(LogLevels.valueOf(config.getString(ConfigurationPaths.StringPaths.GENERAL_LOG_LEVEL)));
            Log.info(String.format("Logging info with level: %s", Log.getLevel()));
        }, "Configuration", String.format("Load config file (%s)", StaticConfiguration.CONFIG_FILENAME), Priorities.HIGHEST, TaskImportance.REQUIRED));

        taskWorker.addTask(new Task(progress -> LocaleManager.load(config.getString(ConfigurationPaths.StringPaths.GENERAL_LANGUAGE)), "Minosoft Language", "Load minosoft language files", Priorities.HIGH, TaskImportance.REQUIRED, "Configuration"));

        taskWorker.addTask(new Task(progress -> {
            Log.info("Loading versions.json...");
            long mappingStartLoadingTime = System.currentTimeMillis();
            Versions.loadAvailableVersions(Util.readJsonAsset("mapping/versions.json"));
            Log.info(String.format("Loaded %d versions in %dms", Versions.getVersionIdMap().size(), (System.currentTimeMillis() - mappingStartLoadingTime)));
            Resources.load();
            Log.info("Loaded all resources!");
        }, "Version mappings", "Load available minecraft versions inclusive mappings", Priorities.NORMAL, TaskImportance.REQUIRED, "Configuration"));

        taskWorker.addTask(new Task(progress -> {
            Log.debug("Refreshing account token...");
            checkClientToken();
            selectAccount(config.getAccounts().get(config.getString(ConfigurationPaths.StringPaths.ACCOUNT_SELECTED)));
        }, "Token refresh", "Refresh selected account token", Priorities.LOW, TaskImportance.OPTIONAL, "Configuration"));

        taskWorker.addTask(new Task(ModLoader::loadMods, "ModLoading", "Load all minosoft mods", Priorities.NORMAL, TaskImportance.REQUIRED, "Configuration"));

        taskWorker.addTask(new Task(progress -> {
            if (!config.getBoolean(ConfigurationPaths.BooleanPaths.NETWORK_SHOW_LAN_SERVERS)) {
                return;
            }
            LANServerListener.listen();
        }, "LAN Server Listener", "Listener for LAN Servers", Priorities.LOWEST, TaskImportance.OPTIONAL, "Configuration"));

        taskWorker.addTask(new Task(progress -> CLI.initialize(), "CLI", "Initialize CLI", Priorities.LOW, TaskImportance.OPTIONAL));

        if (!StaticConfiguration.HEADLESS_MODE) {
            taskWorker.addTask(new Task((progress) -> StartProgressWindow.start(), "JavaFX Toolkit", "Initialize JavaFX", Priorities.HIGHEST));

            taskWorker.addTask(new Task((progress) -> StartProgressWindow.show(START_STATUS_LATCH), "Progress Window", "Display progress window", Priorities.HIGH, TaskImportance.OPTIONAL, "JavaFX Toolkit", "Configuration"));
        }
        taskWorker.work(START_STATUS_LATCH);
        try {
            START_STATUS_LATCH.waitUntilZero();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.info("Everything initialized!");
        if (StaticConfiguration.HEADLESS_MODE) {
            return;
        }
        Launcher.start();
    }

    public static void checkClientToken() {
        if (config.getString(ConfigurationPaths.StringPaths.CLIENT_TOKEN).isBlank()) {
            config.putString(ConfigurationPaths.StringPaths.CLIENT_TOKEN, UUID.randomUUID().toString());
            config.saveToFile();
        }
    }

    public static boolean selectAccount(Account account) {
        if (account == null) {
            config.putString(ConfigurationPaths.StringPaths.ACCOUNT_SELECTED, "");
            config.saveToFile();
            return false;
        }
        if (account.select()) {
            config.putAccount(account);
            config.selectAccount(account);
            config.saveToFile();
            if (Launcher.getMainWindow() != null) {
                Launcher.getMainWindow().selectAccount(account);
            }
            AccountListCell.ACCOUNT_LIST_VIEW.refresh();
            ServerListCell.SERVER_LIST_VIEW.refresh();
            return true;
        }
        account.logout();
        AccountListCell.ACCOUNT_LIST_VIEW.getItems().remove(account);
        config.removeAccount(account);
        config.saveToFile();
        return false;
    }

    public static Configuration getConfig() {
        return config;
    }


    /**
     * Waits until all critical components are started
     */
    public static void waitForStartup() {
        try {
            START_STATUS_LATCH.waitUntilZero();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void shutdown(String message, ShutdownReasons reason) {
        if (isExiting) {
            return;
        }
        if (message == null) {
            message = "";
        }
        if (reason != ShutdownReasons.CLI_HELP && reason != ShutdownReasons.CLI_WRONG_PARAMETER) {
            Log.info("Exiting (reason=%s): %s", reason, message);

            // disconnect from all servers
            for (Object connection : CONNECTIONS.values().toArray()) {
                ((Connection) connection).disconnect();
            }
            Log.info("Disconnected from all connections!");
            if (Thread.currentThread().getName().equals("ShutdownHook")) {
                return;
            }
        }
        isExiting = true;
        System.exit(reason.getExitCode());
    }

    public static void shutdown(ShutdownReasons reason) {
        shutdown(null, reason);
    }

    public static CountUpAndDownLatch getStartStatusLatch() {
        return START_STATUS_LATCH;
    }

}
