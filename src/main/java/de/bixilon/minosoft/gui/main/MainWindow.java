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

import com.jfoenix.controls.JFXAlert;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialogLayout;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.data.locale.LocaleManager;
import de.bixilon.minosoft.data.locale.Strings;
import de.bixilon.minosoft.data.mappings.versions.Versions;
import de.bixilon.minosoft.data.text.BaseComponent;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.protocol.LANServerListener;
import de.bixilon.minosoft.util.DNSUtil;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import javax.annotation.Nullable;
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
            stage.setTitle(LocaleManager.translate(Strings.MANAGE_ACCOUNTS_NO_ACCOUNT_ERROR_TITLE));
            stage.setScene(new Scene(parent));

            GUITools.initializeScene(stage.getScene());
            Platform.setImplicitExit(false);
            stage.setOnCloseRequest(event -> {
                if (Minosoft.getSelectedAccount() == null) {
                    event.consume();
                    JFXAlert<?> alert = new JFXAlert<>();
                    GUITools.initializePane(alert.getDialogPane());
                    alert.setTitle(LocaleManager.translate(Strings.ERROR));
                    JFXDialogLayout layout = new JFXDialogLayout();
                    layout.setHeading(new Label(LocaleManager.translate(Strings.MANAGE_ACCOUNTS_NO_ACCOUNT_ERROR_HEADER)));
                    layout.setBody(new Label(LocaleManager.translate(Strings.MANAGE_ACCOUNTS_NO_ACCOUNT_ERROR_ERROR)));

                    JFXButton cancel = new JFXButton(ButtonType.CANCEL.getText());
                    cancel.setOnAction((actionEvent -> alert.close()));
                    JFXButton close = new JFXButton(ButtonType.OK.getText());
                    close.setOnAction(actionEvent -> System.exit(0));

                    layout.setActions(cancel, close);
                    alert.setContent(layout);
                    alert.showAndWait();
                } else {
                    stage.close();
                }
            });
            stage.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    stage.close();
                }
            });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void selectAccount() {
        if (menuAccount2 == null) {
            return;
        }
        Platform.runLater(() -> {
            if (Minosoft.getSelectedAccount() != null) {
                menuAccount2.setText(LocaleManager.translate(Strings.MAIN_WINDOW_MENU_SERVERS_ACCOUNTS_SELECTED, Minosoft.getSelectedAccount().getPlayerName()));
            } else {
                menuAccount2.setText(LocaleManager.translate(Strings.MAIN_WINDOW_MENU_SERVERS_ACCOUNTS));
            }
        });
    }

    public static void addOrEditServer(@Nullable final Server server) {
        JFXAlert<?> dialog = new JFXAlert<>();
        GUITools.initializePane(dialog.getDialogPane());


        JFXDialogLayout layout = new JFXDialogLayout();

        GridPane gridPane = new GridPane();
        gridPane.setVgap(15);
        gridPane.setHgap(50);

        JFXButton submitButton;


        JFXTextField serverNameField = new JFXTextField();
        serverNameField.setPromptText(LocaleManager.translate(Strings.SERVER_NAME));

        JFXTextField serverAddressField = new JFXTextField();
        serverAddressField.setPromptText(LocaleManager.translate(Strings.SERVER_ADDRESS));
        RequiredFieldValidator serverAddressValidator = new RequiredFieldValidator();
        serverAddressValidator.setMessage(LocaleManager.translate(Strings.SERVER_ADDRESS_INPUT_REQUIRED));
        serverAddressField.getValidators().add(serverAddressValidator);
        serverAddressField.focusedProperty().addListener((o, oldValue, newValue) -> {
            if (!newValue) {
                serverAddressField.validate();
            }
        });

        GUITools.VERSION_COMBO_BOX.getSelectionModel().select(Versions.LOWEST_VERSION_SUPPORTED);


        if (server == null) {
            // add
            dialog.setTitle(LocaleManager.translate(Strings.ADD_SERVER_DIALOG_TITLE));
            layout.setHeading(new Label(LocaleManager.translate(Strings.ADD_SERVER_DIALOG_HEADER)));

            submitButton = new JFXButton(LocaleManager.translate(Strings.BUTTON_ADD));

            serverNameField.setText(LocaleManager.translate(Strings.ADD_SERVER_DIALOG_DEFAULT_SERVER_NAME));
        } else {
            dialog.setTitle(LocaleManager.translate(Strings.EDIT_SERVER_DIALOG_TITLE, server.getName().getMessage()));
            layout.setHeading(new Label(LocaleManager.translate(Strings.EDIT_SERVER_DIALOG_HEADER)));

            submitButton = new JFXButton(LocaleManager.translate(Strings.BUTTON_SAVE));

            serverNameField.setText(server.getName().getLegacyText());
            serverAddressField.setText(server.getAddress());

            if (server.getDesiredVersionId() != -1) {
                GUITools.VERSION_COMBO_BOX.getSelectionModel().select(Versions.getVersionById(server.getDesiredVersionId()));
            }
        }
        submitButton.setButtonType(JFXButton.ButtonType.RAISED);

        gridPane.add(new Label(LocaleManager.translate(Strings.SERVER_NAME) + ":"), 0, 0);
        gridPane.add(serverNameField, 1, 0);
        gridPane.add(new Label(LocaleManager.translate(Strings.SERVER_ADDRESS) + ":"), 0, 1);
        gridPane.add(serverAddressField, 1, 1);
        gridPane.add(new Label(LocaleManager.translate(Strings.VERSION) + ":"), 0, 2);
        gridPane.add(GUITools.VERSION_COMBO_BOX, 1, 2);


        layout.setBody(gridPane);
        JFXButton closeButton = new JFXButton(ButtonType.CLOSE.getText());
        closeButton.setOnAction((actionEvent -> dialog.hide()));
        closeButton.setButtonType(JFXButton.ButtonType.RAISED);
        layout.setActions(closeButton, submitButton);


        serverAddressField.textProperty().addListener((observable, oldValue, newValue) -> submitButton.setDisable(newValue.trim().isEmpty()));
        submitButton.setDisable(serverAddressField.getText().isBlank());
        dialog.setContent(layout);

        Platform.runLater(serverNameField::requestFocus);

        submitButton.setOnAction(actionEvent -> {
            Server server1 = server;
            BaseComponent serverName = new BaseComponent(serverNameField.getText());
            String serverAddress = DNSUtil.correctHostName(serverAddressField.getText());
            int desiredVersionId = GUITools.VERSION_COMBO_BOX.getSelectionModel().getSelectedItem().getVersionId();

            if (server1 == null) {
                server1 = new Server(Server.getNextServerId(), serverName, serverAddress, desiredVersionId);
                Minosoft.serverList.add(server1);
                ServerListCell.listView.getItems().add(server1);
            } else {
                server1.setName(serverName);
                server1.setAddress(serverAddress);
                server1.setDesiredVersionId(desiredVersionId);
                if (server1.getCell() != null) {
                    server1.getCell().setName(server1.getName());
                    //ToDo: version
                }
            }
            server1.saveToConfig();
            Log.info(String.format("%s and saved server (serverName=%s, serverAddress=%s, version=%d)", ((server == null) ? "Added" : "Edited"), serverName.getLegacyText(), serverAddress, desiredVersionId));
            dialog.hide();
        });
        dialog.getDialogPane().setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() != KeyCode.ENTER) {
                return;
            }
            if (serverAddressField.getText().trim().isEmpty()) {
                return;
            }
            submitButton.fire();
        });
        dialog.showAndWait();
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
        addOrEditServer(null);
    }

    @FXML
    public void quit() {
        System.exit(0);
    }

    public void refreshServers() {
        Log.info("Refreshing server list");
        // remove all lan servers
        ServerListCell.listView.getItems().removeAll(LANServerListener.getServers().values());
        LANServerListener.removeAll();

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
            stage.setTitle(LocaleManager.translate(Strings.SETTINGS_TITLE));
            stage.setScene(new Scene(parent));
            GUITools.initializeScene(stage.getScene());
            stage.addEventHandler(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
                if (event.getCode() == KeyCode.ESCAPE) {
                    stage.close();
                }
            });

            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
