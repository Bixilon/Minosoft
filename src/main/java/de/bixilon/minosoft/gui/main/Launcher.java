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
import de.bixilon.minosoft.data.mappings.versions.Version;
import de.bixilon.minosoft.gui.LocaleManager;
import de.bixilon.minosoft.gui.Strings;
import de.bixilon.minosoft.logging.Log;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Dialog;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;

public class Launcher extends Application {
    private static ProgressBar progressBar;
    private static Dialog<Boolean> progressDialog;

    public static void start() {
        Log.info("Starting launcher...");
        launch();
        Log.info("Launcher started!");
    }

    public static void setProgressBar(int jobsLeft) {
        Platform.runLater(() -> {
            if (progressBar == null || progressDialog == null) {
                return;
            }
            if (jobsLeft == 0) {
                progressDialog.setResult(Boolean.TRUE);
                progressDialog.close();
                return;
            }
            progressBar.setProgress(1.0F / jobsLeft);
        });
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        Log.info("Preparing main window...");

        GUITools.versionList.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Version> call(ListView<Version> p) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Version version, boolean empty) {
                        super.updateItem(version, empty);
                        if (!empty && version != null) {
                            setText(String.format("%s (%d)", version.getVersionName(), version.getProtocolVersion()));
                        }
                    }
                };
            }
        });
        ServerListCell.listView.setCellFactory((lv) -> ServerListCell.newInstance());

        ObservableList<Server> servers = FXCollections.observableArrayList();
        servers.addAll(Minosoft.serverList);
        ServerListCell.listView.setItems(servers);

        VBox root = new FXMLLoader(getClass().getResource("/layout/main.fxml")).load();

        Scene scene = new Scene(root, 600, 800);
        primaryStage.setScene(scene);

        primaryStage.setTitle(LocaleManager.translate(Strings.MAIN_WINDOW_TITLE));
        primaryStage.getIcons().add(GUITools.logo);
        primaryStage.show();
        primaryStage.setOnCloseRequest(windowEvent -> System.exit(0));
        if (Minosoft.getSelectedAccount() == null) {
            MainWindow.manageAccounts();
        }
        Log.info("Main window prepared!");
        if (Minosoft.getStartUpJobsLeft() == 0) {
            return;
        }
        progressDialog = new Dialog<>();
        progressDialog.setTitle(LocaleManager.translate(Strings.MINOSOFT_STILL_STARTING_TITLE));
        progressDialog.setHeaderText(LocaleManager.translate(Strings.MINOSOFT_STILL_STARTING_HEADER));
        GridPane grid = new GridPane();
        progressBar = new ProgressBar();
        progressBar.setProgress(1.0D / 5);
        grid.add(progressBar, 0, 0);
        progressDialog.getDialogPane().setContent(grid);
        progressDialog.show();
    }
}