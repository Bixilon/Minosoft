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

package de.bixilon.minosoft.gui.main;

import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.ShutdownReasons;
import de.bixilon.minosoft.data.locale.LocaleManager;
import de.bixilon.minosoft.data.locale.Strings;
import de.bixilon.minosoft.data.mappings.versions.Version;
import de.bixilon.minosoft.protocol.protocol.LANServerListener;
import de.bixilon.minosoft.util.logging.Log;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class Launcher {
    private static Stage stage;
    private static boolean exit;
    private static MainWindow mainWindow;

    public static void start() {
        Log.info("Starting launcher...");
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            if (exit) {
                return;
            }

            GUITools.VERSION_COMBO_BOX.setCellFactory(new Callback<>() {
                @Override
                public ListCell<Version> call(ListView<Version> p) {
                    return new ListCell<>() {
                        @Override
                        protected void updateItem(Version version, boolean empty) {
                            super.updateItem(version, empty);
                            if (!empty && version != null) {
                                setText(String.format("%s (%d)", version.getVersionName(), version.getProtocolId()));
                            }
                        }
                    };
                }
            });
            ServerListCell.SERVER_LIST_VIEW.setCellFactory((lv) -> ServerListCell.newInstance());

            ObservableList<Server> servers = FXCollections.observableArrayList();
            servers.addAll(Minosoft.getConfig().getConfig().getServer().getEntries().values());
            ServerListCell.SERVER_LIST_VIEW.setItems(servers);
            LANServerListener.removeAll(); // remove all LAN Servers

            FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("/layout/main.fxml"));
            VBox root;
            try {
                root = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
                Minosoft.shutdown(e.getMessage(), ShutdownReasons.LAUNCHER_FXML_LOAD_ERROR);
                return;
            }

            Stage stage = new Stage();
            Scene scene = new Scene(root, root.getPrefWidth(), root.getPrefHeight());
            stage.setScene(scene);

            stage.setTitle(LocaleManager.translate(Strings.MAIN_WINDOW_TITLE));
            GUITools.initializeScene(scene);
            stage.setOnCloseRequest(windowEvent -> Minosoft.shutdown(ShutdownReasons.REQUESTED_BY_USER));
            if (exit) {
                return;
            }
            Launcher.stage = stage;
            mainWindow = loader.getController();


            stage.show();
            if (Minosoft.getConfig().getConfig().getAccount().getSelected().isBlank()) {
                MainWindow.manageAccounts();
            }
            latch.countDown();
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.info("Launcher started!");
    }

    public static void exit() {
        exit = true;
        if (stage == null) {
            return;
        }

        Platform.runLater(() -> stage.close());
    }

    public static MainWindow getMainWindow() {
        return mainWindow;
    }
}
