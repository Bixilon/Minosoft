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
import de.bixilon.minosoft.data.mappings.versions.Versions;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.util.DNSUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainWindow implements Initializable {
    public static Menu menuAccount2;

    public BorderPane serversPane;
    public Menu menuFile;
    public MenuItem menuFilePreferences;
    public MenuItem menuFileQuit;
    public Menu menuServers;
    public MenuItem menuServersAdd;
    public MenuItem menuServerRefresh;
    public Menu menuHelp;
    public MenuItem menuHelpAbout;
    public Menu menuAccount;
    public MenuItem menuAccountManage;

    public static void manageAccounts() {
        try {
            Parent parent = new FXMLLoader(MainWindow.class.getResource("/layout/accounts.fxml")).load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Manage accounts - Minosoft");
            stage.setScene(new Scene(parent));
            Platform.setImplicitExit(false);
            stage.setOnCloseRequest(event -> {
                if (Minosoft.getSelectedAccount() == null) {
                    event.consume();
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Error", ButtonType.CANCEL, ButtonType.OK);
                    alert.setHeaderText("Are you sure?");
                    alert.setContentText("No account selected, Minosoft will exit.");
                    alert.showAndWait().ifPresent((type) -> {
                        if (type == ButtonType.OK) {
                            System.exit(0);
                            return;
                        }
                        alert.close();
                    });
                } else {
                    stage.close();
                }
            });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void selectAccount() {
        if (Minosoft.getSelectedAccount() != null) {
            menuAccount2.setText(LocaleManager.translate(Strings.MAIN_WINDOW_MENU_SERVERS_ACCOUNTS_SELECTED, Minosoft.getSelectedAccount().getPlayerName()));
        } else {
            menuAccount2.setText(LocaleManager.translate(Strings.MAIN_WINDOW_MENU_SERVERS_ACCOUNTS));
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serversPane.setCenter(ServerListCell.listView);

        menuAccount2 = menuAccount;
        menuFile.setText(LocaleManager.translate(Strings.MAIN_WINDOW_MENU_FILE));
        menuFilePreferences.setText(LocaleManager.translate(Strings.MAIN_WINDOW_MENU_FILE_PREFERENCES));
        menuFileQuit.setText(LocaleManager.translate(Strings.MAIN_WINDOW_MENU_FILE_QUIT));
        menuServers.setText(LocaleManager.translate(Strings.MAIN_WINDOW_MENU_SERVERS));
        menuServersAdd.setText(LocaleManager.translate(Strings.MAIN_WINDOW_MENU_SERVERS_ADD));
        menuServerRefresh.setText(LocaleManager.translate(Strings.MAIN_WINDOW_MENU_SERVERS_REFRESH));
        menuHelp.setText(LocaleManager.translate(Strings.MAIN_WINDOW_MENU_SERVERS_HELP));
        menuHelpAbout.setText(LocaleManager.translate(Strings.MAIN_WINDOW_MENU_SERVERS_HELP_ABOUT));
        menuAccountManage.setText(LocaleManager.translate(Strings.MAIN_WINDOW_MENU_SERVERS_ACCOUNTS_MANAGE));
        selectAccount();
    }

    @FXML
    public void addServer() {
        Dialog<?> dialog = new Dialog<>();
        dialog.setTitle(LocaleManager.translate(Strings.ADD_SERVER_DIALOG_TITLE));
        dialog.setHeaderText(LocaleManager.translate(Strings.ADD_SERVER_DIALOG_HEADER));

        ButtonType addButtonType = new ButtonType(LocaleManager.translate(Strings.BUTTON_ADD), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 300, 10, 10));

        TextField serverName = new TextField();
        serverName.setPromptText(LocaleManager.translate(Strings.SERVER_NAME));
        serverName.setText(LocaleManager.translate(Strings.ADD_SERVER_DIALOG_DEFAULT_SERVER_NAME));
        TextField serverAddress = new TextField();
        serverAddress.setPromptText(LocaleManager.translate(Strings.SERVER_ADDRESS));

        GUITools.versionList.getSelectionModel().select(Versions.getLowestVersionSupported());

        grid.add(new Label(LocaleManager.translate(Strings.SERVER_NAME) + ":"), 0, 0);
        grid.add(serverName, 1, 0);
        grid.add(new Label(LocaleManager.translate(Strings.SERVER_ADDRESS) + ":"), 0, 1);
        grid.add(serverAddress, 1, 1);
        grid.add(new Label(LocaleManager.translate(Strings.VERSION) + ":"), 0, 2);
        grid.add(GUITools.versionList, 1, 2);

        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);

        serverAddress.textProperty().addListener((observable, oldValue, newValue) -> addButton.setDisable(newValue.trim().isEmpty()));
        addButton.setDisable(true);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(serverName::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Server server = new Server(Server.getNextServerId(), serverName.getText(), DNSUtil.correctHostName(serverAddress.getText()), GUITools.versionList.getSelectionModel().getSelectedItem().getVersionId());
                Minosoft.serverList.add(server);
                server.saveToConfig();
                ServerListCell.listView.getItems().add(server);
                Log.info(String.format("Added and saved server (serverName=%s, serverAddress=%s, version=%d)", server.getName(), server.getAddress(), server.getDesiredVersionId()));
            }
            return null;
        });
        dialog.showAndWait();
    }

    @FXML
    public void quit() {
        System.exit(0);
    }

    public void refreshServers() {
        Log.info("Refreshing server list");
        for (Server server : ServerListCell.listView.getItems()) {
            if (server.getLastPing() == null) {
                // server was not pinged, don't even try, only costs memory and cpu
                continue;
            }
            server.ping();
            ServerListCell.listView.refresh();
        }
    }

    public void manageAccounts(ActionEvent actionEvent) {
        manageAccounts();
    }

    public void openSettings() {
        try {
            Parent parent = new FXMLLoader(MainWindow.class.getResource("/layout/settings.fxml")).load();
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(GUITools.logo);
            stage.setTitle(LocaleManager.translate(Strings.SETTINGS_TITLE));
            stage.setScene(new Scene(parent));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
