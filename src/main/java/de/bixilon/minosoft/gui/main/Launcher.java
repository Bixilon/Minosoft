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

package de.bixilon.minosoft.gui.main;

import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.data.locale.LocaleManager;
import de.bixilon.minosoft.data.locale.Strings;
import de.bixilon.minosoft.data.mappings.versions.Version;
import de.bixilon.minosoft.logging.Log;
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
    private static boolean exit = false;

    public static void start() {
        Log.info("Starting launcher...");
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            if (exit) {
                return;
            }
            Stage stage = new Stage();
            stage.getIcons().add(GUITools.logo);

            GUITools.versionList.setCellFactory(new Callback<>() {
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
            ServerListCell.listView.setCellFactory((lv) -> ServerListCell.newInstance());

            ObservableList<Server> servers = FXCollections.observableArrayList();
            servers.addAll(Minosoft.serverList);
            ServerListCell.listView.setItems(servers);

            VBox root = null;
            try {
                root = new FXMLLoader(Launcher.class.getResource("/layout/main.fxml")).load();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            Scene scene = new Scene(root, 600, 800);
            stage.setScene(scene);

            stage.setTitle(LocaleManager.translate(Strings.MAIN_WINDOW_TITLE));
            stage.getIcons().add(GUITools.logo);
            stage.setOnCloseRequest(windowEvent -> System.exit(0));
            if (exit) {
                return;
            }
            stage.show();
            Launcher.stage = stage;
            if (Minosoft.getSelectedAccount() == null) {
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

    public static Stage getStage() {
        return stage;
    }
}