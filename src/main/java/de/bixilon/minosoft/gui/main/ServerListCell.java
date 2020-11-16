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
import de.bixilon.minosoft.data.Player;
import de.bixilon.minosoft.data.locale.LocaleManager;
import de.bixilon.minosoft.data.locale.Strings;
import de.bixilon.minosoft.data.mappings.versions.Version;
import de.bixilon.minosoft.data.mappings.versions.Versions;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.ping.ForgeModInfo;
import de.bixilon.minosoft.protocol.ping.ServerListPing;
import de.bixilon.minosoft.util.DNSUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class ServerListCell extends ListCell<Server> implements Initializable {
    public static final ListView<Server> listView = new ListView<>();

    public ImageView icon;
    public TextFlow motd;
    public Label version;
    public Label players;
    public Label serverBrand;
    public Label serverName;
    public AnchorPane root;
    public MenuItem optionsConnect;
    public MenuItem optionsShowInfo;
    public MenuItem optionsEdit;
    public MenuItem optionsRefresh;
    public MenuItem optionsSessions;
    public MenuItem optionsDelete;
    public MenuButton optionsMenu;

    boolean canConnect = false;
    private Server server;

    public static ServerListCell newInstance() {
        FXMLLoader loader = new FXMLLoader(ServerListCell.class.getResource("/layout/cells/server.fxml"));
        try {
            loader.load();
            return loader.getController();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        updateSelected(false);
        setGraphic(root);

        // change locale
        optionsConnect.setText(LocaleManager.translate(Strings.SERVER_ACTION_CONNECT));
        optionsShowInfo.setText(LocaleManager.translate(Strings.SERVER_ACTION_SHOW_INFO));
        optionsEdit.setText(LocaleManager.translate(Strings.SERVER_ACTION_EDIT));
        optionsRefresh.setText(LocaleManager.translate(Strings.SERVER_ACTION_REFRESH));
        optionsSessions.setText(LocaleManager.translate(Strings.SERVER_ACTION_SESSIONS));
        optionsDelete.setText(LocaleManager.translate(Strings.SERVER_ACTION_DELETE));
    }

    public AnchorPane getRoot() {
        return root;
    }

    @Override
    protected void updateItem(Server server, boolean empty) {
        super.updateItem(server, empty);

        root.setVisible(!empty);
        if (empty) {
            return;
        }

        if (server == null) {
            return;
        }

        if (server.equals(this.server)) {
            return;
        }
        resetCell();

        this.server = server;
        serverName.setText(server.getName());

        Image favicon = GUITools.getImage(server.getFavicon());
        if (favicon == null) {
            favicon = GUITools.logo;
        }
        icon.setImage(favicon);
        if (server.isConnected()) {
            setStyle("-fx-background-color: darkseagreen;");
            optionsSessions.setDisable(false);
        } else {
            optionsSessions.setDisable(true);
        }
        if (server.getLastPing() == null) {
            server.ping();
        }
        server.getLastPing().addPingCallback(ping -> Platform.runLater(() -> {
            if (server != this.server) {
                // cell does not contains us anymore
                return;
            }
            resetCell();

            if (server.isConnected()) {
                setStyle("-fx-background-color: darkseagreen;");
            }
            if (ping == null) {
                // Offline
                players.setText("");
                version.setText(LocaleManager.translate(Strings.OFFLINE));
                version.setStyle("-fx-text-fill: red;");
                setErrorMotd(String.format("%s", server.getLastPing().getLastConnectionException()));
                optionsConnect.setDisable(true);
                canConnect = false;
                return;
            }
            players.setText(LocaleManager.translate(Strings.SERVER_INFO_SLOTS_PLAYERS_ONLINE, ping.getPlayerOnline(), ping.getMaxPlayers()));
            Version serverVersion;
            if (server.getDesiredVersionId() == -1) {
                serverVersion = Versions.getVersionByProtocolId(ping.getProtocolId());
            } else {
                serverVersion = Versions.getVersionById(server.getDesiredVersionId());
                version.setStyle("-fx-text-fill: green;");
            }
            if (serverVersion == null) {
                version.setText(ping.getServerBrand());
                version.setStyle("-fx-text-fill: red;");
                optionsConnect.setDisable(true);
                canConnect = false;
            } else {
                version.setText(serverVersion.getVersionName());
                optionsConnect.setDisable(false);
                canConnect = true;
            }
            serverBrand.setText(ping.getServerModInfo().getBrand());
            serverBrand.setTooltip(new Tooltip(ping.getServerModInfo().getInfo()));
            motd.getChildren().addAll(ping.getMotd().getJavaFXText(FXCollections.observableArrayList()));
            if (ping.getFavicon() != null) {
                icon.setImage(GUITools.getImage(ping.getFavicon()));
                if (!Arrays.equals(ping.getFavicon(), server.getFavicon())) {
                    server.setFavicon(ping.getFavicon());
                    server.saveToConfig();
                }
            }
            if (server.isReadOnly()) {
                optionsEdit.setDisable(true);
                optionsDelete.setDisable(true);
            }
            if (server.getLastPing().getLastConnectionException() != null) {
                // connection failed because of an error in minosoft, but ping was okay
                version.setStyle("-fx-text-fill: red;");
                optionsConnect.setDisable(true);
                canConnect = false;
                setErrorMotd(String.format("%s: %s", server.getLastPing().getLastConnectionException().getClass().getCanonicalName(), server.getLastPing().getLastConnectionException().getLocalizedMessage()));
            }
        }));
    }

    private void resetCell() {
        // clear all cells
        setStyle(null);
        motd.getChildren().clear();
        serverBrand.setText("");
        serverBrand.setTooltip(null);
        motd.setStyle(null);
        version.setText(LocaleManager.translate(Strings.CONNECTING));
        version.setStyle(null);
        players.setText("");
        optionsConnect.setDisable(true);
        optionsEdit.setDisable(false);
        optionsDelete.setDisable(false);
    }

    private void setErrorMotd(String message) {
        motd.getChildren().clear();
        Text text = new Text(message);
        text.setFill(Color.RED);
        motd.getChildren().add(text);
    }

    public void delete() {
        if (server.isReadOnly()) {
            return;
        }
        server.getConnections().forEach(Connection::disconnect);
        server.delete();
        Log.info(String.format("Deleted server (name=\"%s\", address=\"%s\")", server.getName(), server.getAddress()));
        listView.getItems().remove(server);
    }

    public void refresh() {
        Log.info(String.format("Refreshing server status (serverName=\"%s\", address=\"%s\")", server.getName(), server.getAddress()));
        if (server.getLastPing() == null) {
            // server was not pinged, don't even try, only costs memory and cpu
            return;
        }
        server.ping();
    }

    public void clicked(MouseEvent e) {
        switch (e.getButton()) {
            case PRIMARY -> {
                if (e.getClickCount() == 2) {
                    connect();
                }
            }
            case SECONDARY -> optionsMenu.fire();
            case MIDDLE -> {
                listView.getSelectionModel().select(server);
                edit();
            }
        }
    }

    public void connect() {
        if (!canConnect || server.getLastPing() == null) {
            return;
        }
        Connection connection = new Connection(Connection.lastConnectionId++, server.getAddress(), new Player(Minosoft.getSelectedAccount()));
        Version version;
        if (server.getDesiredVersionId() == -1) {
            version = server.getLastPing().getVersion();
        } else {
            version = Versions.getVersionById(server.getDesiredVersionId());
        }
        optionsConnect.setDisable(true);
        connection.connect(server.getLastPing().getAddress(), version);
        connection.addConnectionChangeCallback(this::handleConnectionCallback);
        server.addConnection(connection);

    }

    public void edit() {
        if (server.isReadOnly()) {
            return;
        }
        Dialog<?> dialog = new Dialog<>();
        dialog.setTitle(LocaleManager.translate(Strings.EDIT_SERVER_DIALOG_TITLE, server.getName()));
        dialog.setHeaderText(LocaleManager.translate(Strings.EDIT_SERVER_DIALOG_HEADER));
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(GUITools.logo);

        ButtonType saveButtonType = new ButtonType(LocaleManager.translate(Strings.BUTTON_SAVE), ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 300, 10, 10));

        TextField serverName = new TextField();
        serverName.setPromptText(LocaleManager.translate(Strings.SERVER_NAME));
        serverName.setText(server.getName());
        TextField serverAddress = new TextField();
        serverAddress.setPromptText(LocaleManager.translate(Strings.SERVER_ADDRESS));
        serverAddress.setText(server.getAddress());

        if (server.getDesiredVersionId() == -1) {
            GUITools.versionList.getSelectionModel().select(Versions.LOWEST_VERSION_SUPPORTED);
        } else {
            GUITools.versionList.getSelectionModel().select(Versions.getVersionById(server.getDesiredVersionId()));
        }

        grid.add(new Label(LocaleManager.translate(Strings.SERVER_NAME) + ":"), 0, 0);
        grid.add(serverName, 1, 0);
        grid.add(new Label(LocaleManager.translate(Strings.SERVER_ADDRESS) + ":"), 0, 1);
        grid.add(serverAddress, 1, 1);
        grid.add(new Label(LocaleManager.translate(Strings.VERSION) + ":"), 0, 2);
        grid.add(GUITools.versionList, 1, 2);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);

        serverAddress.textProperty().addListener((observable, oldValue, newValue) -> saveButton.setDisable(newValue.trim().isEmpty()));

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(serverName::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                serverName.setText(serverName.getText());
                server.setName(serverName.getText());
                server.setDesiredVersionId(GUITools.versionList.getSelectionModel().getSelectedItem().getVersionId());
                if (server.getDesiredVersionId() != -1) {
                    version.setText(Versions.getVersionById(server.getDesiredVersionId()).getVersionName());
                    version.setTextFill(Color.BLACK);
                }
                server.setAddress(DNSUtil.correctHostName(serverAddress.getText()));
                server.saveToConfig();
                Log.info(String.format("Edited and saved server (serverName=%s, serverAddress=%s, version=%d)", server.getName(), server.getAddress(), server.getDesiredVersionId()));
            }
            return null;
        });

        dialog.showAndWait();
    }

    private void handleConnectionCallback(Connection connection) {
        if (!server.getConnections().contains(connection)) {
            // the card got recycled
            return;
        }
        Platform.runLater(() -> {
            if (!connection.isConnected()) {
                // maybe we got disconnected
                if (!server.isConnected()) {
                    setStyle(null);
                    optionsSessions.setDisable(true);
                    optionsConnect.setDisable(false);
                }
                return;
            }

            if (Minosoft.getSelectedAccount() != connection.getPlayer().getAccount()) {
                optionsConnect.setDisable(false);
            }
            setStyle("-fx-background-color: darkseagreen;");
            optionsSessions.setDisable(false);
        });
    }

    public void showInfo() {
        Dialog<?> dialog = new Dialog<>();
        dialog.setTitle("View server info: " + server.getName());
        ((Stage) dialog.getDialogPane().getScene().getWindow()).getIcons().add(GUITools.logo);

        ButtonType loginButtonType = ButtonType.CLOSE;
        dialog.getDialogPane().getButtonTypes().add(loginButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 300, 10, 10));

        Label serverNameLabel = new Label(server.getName());
        Label serverAddressLabel = new Label(server.getAddress());
        Label forcedVersionLabel = new Label();

        if (server.getDesiredVersionId() == -1) {
            forcedVersionLabel.setText(Versions.LOWEST_VERSION_SUPPORTED.getVersionName());
        } else {
            forcedVersionLabel.setText(Versions.getVersionById(server.getDesiredVersionId()).getVersionName());
        }

        int column = -1;
        grid.add(new Label(LocaleManager.translate(Strings.SERVER_NAME) + ":"), 0, ++column);
        grid.add(serverNameLabel, 1, column);
        grid.add(new Label(LocaleManager.translate(Strings.SERVER_ADDRESS) + ":"), 0, ++column);
        grid.add(serverAddressLabel, 1, column);
        grid.add(new Label(LocaleManager.translate(Strings.FORCED_VERSION) + ":"), 0, ++column);
        grid.add(forcedVersionLabel, 1, column);

        if (server.getLastPing() != null) {
            if (server.getLastPing().getLastConnectionException() != null) {
                Label lastConnectionExceptionLabel = new Label(server.getLastPing().getLastConnectionException().toString());
                lastConnectionExceptionLabel.setStyle("-fx-text-fill: red");
                grid.add(new Label(LocaleManager.translate(Strings.SERVER_INFO_LAST_CONNECTION_EXCEPTION) + ":"), 0, ++column);
                grid.add(lastConnectionExceptionLabel, 1, column);
            }

            if (server.getLastPing().getLastPing() != null) {
                ServerListPing lastPing = server.getLastPing().getLastPing();
                Version serverVersion = Versions.getVersionByProtocolId(lastPing.getProtocolId());
                String serverVersionString;
                if (serverVersion == null) {
                    serverVersionString = LocaleManager.translate(Strings.SERVER_INFO_VERSION_UNKNOWN, lastPing.getProtocolId());
                } else {
                    serverVersionString = serverVersion.getVersionName();
                }
                Label realServerAddressLabel = new Label(server.getLastPing().getAddress().toString());
                Label serverVersionLabel = new Label(serverVersionString);
                Label serverBrandLabel = new Label(lastPing.getServerBrand());
                Label playersOnlineMaxLabel = new Label(LocaleManager.translate(Strings.SERVER_INFO_SLOTS_PLAYERS_ONLINE, lastPing.getPlayerOnline(), lastPing.getMaxPlayers()));
                TextFlow motdLabel = new TextFlow();
                motdLabel.getChildren().addAll(lastPing.getMotd().getJavaFXText(FXCollections.observableArrayList()));
                Label moddedBrandLabel = new Label(lastPing.getServerModInfo().getBrand());

                grid.add(new Label(LocaleManager.translate(Strings.SERVER_INFO_REAL_SERVER_ADDRESS) + ":"), 0, ++column);
                grid.add(realServerAddressLabel, 1, column);
                grid.add(new Label(LocaleManager.translate(Strings.VERSION) + ":"), 0, ++column);
                grid.add(serverVersionLabel, 1, column);
                grid.add(new Label(LocaleManager.translate(Strings.SERVER_INFO_SERVER_BRAND) + ":"), 0, ++column);
                grid.add(serverBrandLabel, 1, column);
                grid.add(new Label(LocaleManager.translate(Strings.SERVER_INFO_PLAYERS_ONLINE) + ":"), 0, ++column);
                grid.add(playersOnlineMaxLabel, 1, column);
                grid.add(new Label(LocaleManager.translate(Strings.SERVER_INFO_MESSAGE_OF_THE_DAY) + ":"), 0, ++column);
                grid.add(motdLabel, 1, column);
                grid.add(new Label(LocaleManager.translate(Strings.SERVER_INFO_SERVER_MODDED_BRAND) + ":"), 0, ++column);
                grid.add(moddedBrandLabel, 1, column);

                if (lastPing.getServerModInfo() instanceof ForgeModInfo forgeModInfo) {
                    Label moddedModsLabel = new Label(forgeModInfo.getModList().toString());
                    moddedModsLabel.setWrapText(true);

                    grid.add(new Label(LocaleManager.translate(Strings.SERVER_INFO_SERVER_MODDED_MOD_LIST) + ":"), 0, ++column);
                    grid.add(moddedModsLabel, 1, column);
                }
            }
        }

        dialog.getDialogPane().setContent(grid);

        dialog.showAndWait();
    }

    public void manageSessions() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/layout/sessions.fxml"));
            Parent parent = loader.load();
            ((SessionsWindow) loader.getController()).setServer(server);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(LocaleManager.translate(Strings.SESSIONS_DIALOG_TITLE, server.getName()));
            stage.getIcons().add(GUITools.logo);
            stage.setScene(new Scene(parent));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
