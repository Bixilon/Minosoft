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
import de.bixilon.minosoft.config.StaticConfiguration;
import de.bixilon.minosoft.data.accounts.Account;
import de.bixilon.minosoft.data.assets.JarAssetsManager;
import de.bixilon.minosoft.data.assets.Resources;
import de.bixilon.minosoft.data.locale.LocaleManager;
import de.bixilon.minosoft.data.mappings.DefaultRegistries;
import de.bixilon.minosoft.data.mappings.ResourceLocation;
import de.bixilon.minosoft.data.mappings.versions.Versions;
import de.bixilon.minosoft.gui.main.GUITools;
import de.bixilon.minosoft.gui.main.Launcher;
import de.bixilon.minosoft.gui.main.ServerListCell;
import de.bixilon.minosoft.gui.main.StartProgressWindow;
import de.bixilon.minosoft.gui.main.cells.AccountListCell;
import de.bixilon.minosoft.modding.event.EventManager;
import de.bixilon.minosoft.modding.loading.ModLoader;
import de.bixilon.minosoft.modding.loading.Priorities;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.protocol.LANServerListener;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.terminal.CLI;
import de.bixilon.minosoft.util.*;
import de.bixilon.minosoft.util.logging.Log;
import de.bixilon.minosoft.util.task.AsyncTaskWorker;
import de.bixilon.minosoft.util.task.Task;
import de.bixilon.minosoft.util.task.TaskImportance;
import javafx.application.Platform;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Minosoft {
    public static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(MMath.INSTANCE.clamp(Runtime.getRuntime().availableProcessors() - 1, 2, 16), Util.getThreadFactory("Worker"));
    public static final JarAssetsManager MINOSOFT_ASSETS_MANAGER = new JarAssetsManager(Minosoft.class, Set.of("minosoft"));
    public static final HashSet<EventManager> EVENT_MANAGERS = new HashSet<>();
    public static final HashBiMap<Integer, PlayConnection> CONNECTIONS = HashBiMap.create();
    private static final CountUpAndDownLatch START_STATUS_LATCH = new CountUpAndDownLatch(1);
    public static Configuration config;
    private static boolean isExiting;

    public static void main(String[] args) {
        MinosoftCommandLineArguments.parseCommandLineArguments(args);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> shutdown(ShutdownReasons.UNKNOWN), "ShutdownHook"));
        Util.initUtilClasses();

        Log.info("Starting...");
        GitInfo.INSTANCE.load();
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
            } catch (Exception e) {
                Log.fatal("Failed to load config file!");
                throw e;
            }
            Log.info(String.format("Loaded config file (version=%s)", config.getConfig().getGeneral().getVersion()));
            // set log level from config
            Log.setLevel(config.getConfig().getGeneral().getLogLevel());
            Log.info(String.format("Logging info with level: %s", Log.getLevel()));
        }, "Configuration", String.format("Load config file (%s)", StaticConfiguration.CONFIG_FILENAME), Priorities.HIGHEST, TaskImportance.REQUIRED));

        taskWorker.addTask(new Task(progress -> LocaleManager.load(config.getConfig().getGeneral().getLanguage()), "Minosoft Language", "Load minosoft language files", Priorities.HIGH, TaskImportance.REQUIRED, "Configuration"));

        taskWorker.addTask(new Task(progress -> {
            Log.info("Loading versions.json...");
            long mappingStartLoadingTime = System.currentTimeMillis();
            Versions.loadAvailableVersions(MINOSOFT_ASSETS_MANAGER.readJsonAsset(new ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "mapping/versions.json")));
            Log.info(String.format("Loaded %d versions in %dms", Versions.getVersionIdMap().size(), (System.currentTimeMillis() - mappingStartLoadingTime)));
            Log.info("Loading resources...");
            Resources.load();
            Log.info("Loaded resources!");
            Log.info("Loading default registries...");
            DefaultRegistries.INSTANCE.load();
            Log.info("Loaded default registries!");
        }, "Version mappings", "Load available minecraft versions inclusive mappings", Priorities.NORMAL, TaskImportance.REQUIRED, "Configuration"));

        taskWorker.addTask(new Task(progress -> {
            Log.debug("Refreshing account token...");
            selectAccount(config.getConfig().getAccount().getEntries().get(config.getConfig().getAccount().getSelected()));
        }, "Token refresh", "Refresh selected account token", Priorities.LOW, TaskImportance.OPTIONAL, "Configuration"));

        taskWorker.addTask(new Task(ModLoader::loadMods, "ModLoading", "Load all minosoft mods", Priorities.NORMAL, TaskImportance.REQUIRED, "Configuration"));

        taskWorker.addTask(new Task(progress -> {
            if (!config.getConfig().getNetwork().getShowLanServers()) {
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

    public static boolean selectAccount(Account account) {
        if (account == null) {
            config.getConfig().getAccount().setSelected("");
            config.saveToFile();
            return false;
        }
        if (account.select()) {
            config.getConfig().getAccount().getEntries().put(account.getId(), account);
            config.getConfig().getAccount().setSelected(account.getId());
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
        config.getConfig().getAccount().getEntries().remove(account.getId());
        config.saveToFile();
        return false;
    }

    public static Configuration getConfig() {
        return config;
    }

    public static void shutdown(String message, ShutdownReasons reason) {
        if (isExiting) {
            return;
        }
        if (message == null) {
            message = "";
        }
        if (reason != ShutdownReasons.CLI_HELP && reason != ShutdownReasons.CLI_WRONG_PARAMETER) {
            String logMessage = String.format("Exiting: %s", reason);
            if (!message.isBlank()) {
                logMessage += String.format(": %s", message);
            }
            Log.info(logMessage);

            if (!CONNECTIONS.isEmpty()) {
                // disconnect from all servers
                for (Object connection : CONNECTIONS.values().toArray()) {
                    ((PlayConnection) connection).disconnect();
                }
                Log.info("Disconnected from all connections!");
            }
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
}
