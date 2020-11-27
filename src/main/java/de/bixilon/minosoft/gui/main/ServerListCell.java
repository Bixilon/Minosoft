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
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.data.Player;
import de.bixilon.minosoft.data.locale.LocaleManager;
import de.bixilon.minosoft.data.locale.Strings;
import de.bixilon.minosoft.data.mappings.versions.Version;
import de.bixilon.minosoft.data.mappings.versions.Versions;
import de.bixilon.minosoft.data.text.BaseComponent;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.modding.event.EventInvokerCallback;
import de.bixilon.minosoft.modding.event.events.ConnectionStateChangeEvent;
import de.bixilon.minosoft.modding.event.events.ServerListPingArriveEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.ping.ForgeModInfo;
import de.bixilon.minosoft.protocol.ping.ServerListPing;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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

    public ImageView faviconField;
    public TextFlow nameField;
    public TextFlow motdField;
    public Label versionField;
    public Label playersField;
    public Label brandField;

    public GridPane root;
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
        server.setCell(this);
        resetCell();

        this.server = server;
        setName(server.getName());

        Image favicon = GUITools.getImage(server.getFavicon());
        if (favicon == null) {
            favicon = GUITools.MINOSOFT_LOGO;
        }
        faviconField.setImage(favicon);
        if (server.isConnected()) {
            setStyle("-fx-background-color: darkseagreen;");
            optionsSessions.setDisable(false);
        } else {
            optionsSessions.setDisable(true);
        }
        if (server.getLastPing() == null) {
            server.ping();
        }
        server.getLastPing().registerEvent(new EventInvokerCallback<ServerListPingArriveEvent>(ServerListPingArriveEvent.class, event -> Platform.runLater(() -> {
            ServerListPing ping = event.getServerListPing();
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
                playersField.setText("");
                versionField.setText(LocaleManager.translate(Strings.OFFLINE));
                versionField.setStyle("-fx-text-fill: red;");
                setErrorMotd(String.format("%s", server.getLastPing().getLastConnectionException()));
                optionsConnect.setDisable(true);
                canConnect = false;
                return;
            }
            playersField.setText(LocaleManager.translate(Strings.SERVER_INFO_SLOTS_PLAYERS_ONLINE, ping.getPlayerOnline(), ping.getMaxPlayers()));
            Version serverVersion;
            if (server.getDesiredVersionId() == -1) {
                serverVersion = Versions.getVersionByProtocolId(ping.getProtocolId());
            } else {
                serverVersion = Versions.getVersionById(server.getDesiredVersionId());
                versionField.setStyle("-fx-text-fill: -secondary-light-light-color;");
            }
            if (serverVersion == null) {
                versionField.setText(ping.getServerBrand());
                versionField.setStyle("-fx-text-fill: red;");
                optionsConnect.setDisable(true);
                canConnect = false;
            } else {
                versionField.setText(serverVersion.getVersionName());
                optionsConnect.setDisable(false);
                canConnect = true;
            }
            brandField.setText(ping.getServerModInfo().getBrand());
            brandField.setTooltip(new Tooltip(ping.getServerModInfo().getInfo()));
            motdField.getChildren().setAll(ping.getMotd().getJavaFXText());
            if (ping.getFavicon() != null) {
                faviconField.setImage(GUITools.getImage(ping.getFavicon()));
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
                versionField.setStyle("-fx-text-fill: red;");
                optionsConnect.setDisable(true);
                canConnect = false;
                setErrorMotd(String.format("%s: %s", server.getLastPing().getLastConnectionException().getClass().getCanonicalName(), server.getLastPing().getLastConnectionException().getMessage()));
            }
        })));
    }

    public void setName(BaseComponent name) {
        nameField.getChildren().setAll(name.getJavaFXText());
        for (Node node : nameField.getChildren()) {
            node.setStyle("-fx-font-size: 15pt ;");
        }
    }

    private void resetCell() {
        // clear all cells
        setStyle(null);
        motdField.getChildren().clear();
        brandField.setText("");
        brandField.setTooltip(null);
        motdField.setStyle(null);
        versionField.setText(LocaleManager.translate(Strings.CONNECTING));
        versionField.setStyle(null);
        playersField.setText("");
        optionsConnect.setDisable(true);
        optionsEdit.setDisable(false);
        optionsDelete.setDisable(false);
    }

    private void setErrorMotd(String message) {
        Text text = new Text(message);
        text.setFill(Color.RED);
        motdField.getChildren().setAll(text);
    }

    public void delete() {
        if (server.isReadOnly()) {
            return;
        }
        server.getConnections().forEach(Connection::disconnect);
        server.delete();
        Log.info(String.format("Deleted server (name=\"%s\", address=\"%s\")", server.getName().getLegacyText(), server.getAddress()));
        listView.getItems().remove(server);
    }

    public void refresh() {
        Log.info(String.format("Refreshing server status (serverName=\"%s\", address=\"%s\")", server.getName().getLegacyText(), server.getAddress()));
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
                editServer();
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
        connection.registerEvent(new EventInvokerCallback<>(this::handleConnectionCallback));
        server.addConnection(connection);

    }

    public void editServer() {
        MainWindow.addOrEditServer(server);
    }

    private void handleConnectionCallback(ConnectionStateChangeEvent event) {
        Connection connection = event.getConnection();
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
        JFXAlert<?> dialog = new JFXAlert<>();
        dialog.setTitle("View server info: " + server.getName().getMessage());
        GUITools.initializePane(dialog.getDialogPane());

        JFXDialogLayout layout = new JFXDialogLayout();


        JFXButton closeButton = new JFXButton(ButtonType.CLOSE.getText());
        closeButton.setOnAction((actionEvent -> dialog.hide()));
        closeButton.setButtonType(JFXButton.ButtonType.RAISED);
        layout.setActions(closeButton);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextFlow serverNameLabel = new TextFlow();
        serverNameLabel.getChildren().setAll(server.getName().getJavaFXText());
        Label serverAddressLabel = new Label(server.getAddress());
        Label forcedVersionLabel = new Label();

        if (server.getDesiredVersionId() == -1) {
            forcedVersionLabel.setText(Versions.LOWEST_VERSION_SUPPORTED.getVersionName());
        } else {
            forcedVersionLabel.setText(Versions.getVersionById(server.getDesiredVersionId()).getVersionName());
        }

        int column = -1;
        Label a = new Label(LocaleManager.translate(Strings.SERVER_NAME) + ":");
        a.setWrapText(false);
        grid.add(a, 0, ++column);
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
                motdLabel.getChildren().setAll(lastPing.getMotd().getJavaFXText());
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
        // ToDo: size probably
        layout.setBody(grid);
        dialog.setContent(layout);
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
            stage.setScene(new Scene(parent));
            GUITools.initializeScene(stage.getScene());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
