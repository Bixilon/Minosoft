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
import de.bixilon.minosoft.data.player.PingBars;
import de.bixilon.minosoft.data.text.BaseComponent;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.modding.event.EventInvokerCallback;
import de.bixilon.minosoft.modding.event.events.ConnectionStateChangeEvent;
import de.bixilon.minosoft.modding.event.events.ServerListPongEvent;
import de.bixilon.minosoft.modding.event.events.ServerListStatusArriveEvent;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.ping.ForgeModInfo;
import de.bixilon.minosoft.protocol.ping.ServerListPing;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.ResourceBundle;

public class ServerListCell extends ListCell<Server> implements Initializable {
    public static final ListView<Server> SERVER_LIST_VIEW = new ListView<>();

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
    public Label pingField;

    boolean canConnect;
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
        setGraphic(this.root);

        // change locale
        this.optionsConnect.setText(LocaleManager.translate(Strings.SERVER_ACTION_CONNECT));
        this.optionsShowInfo.setText(LocaleManager.translate(Strings.SERVER_ACTION_SHOW_INFO));
        this.optionsEdit.setText(LocaleManager.translate(Strings.SERVER_ACTION_EDIT));
        this.optionsRefresh.setText(LocaleManager.translate(Strings.SERVER_ACTION_REFRESH));
        this.optionsSessions.setText(LocaleManager.translate(Strings.SERVER_ACTION_SESSIONS));
        this.optionsDelete.setText(LocaleManager.translate(Strings.SERVER_ACTION_DELETE));
    }

    @Override
    protected void updateItem(Server server, boolean empty) {
        super.updateItem(server, empty);

        this.root.setVisible(server != null || !empty);
        if (empty) {
            resetCell();
            return;
        }

        if (server == null) {
            resetCell();
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
        this.faviconField.setImage(favicon);
        if (server.isConnected()) {
            getStyleClass().add("list-cell-connected");
            this.optionsSessions.setDisable(false);
        } else {
            this.optionsSessions.setDisable(true);
        }
        if (server.getLastPing() == null) {
            server.ping();
        }
        server.getLastPing().registerEvent(new EventInvokerCallback<ServerListStatusArriveEvent>(ServerListStatusArriveEvent.class, event -> Platform.runLater(() -> {
            ServerListPing ping = event.getServerListPing();
            if (server != this.server) {
                // cell does not contains us anymore
                return;
            }
            resetCell();

            if (server.isConnected()) {
                getStyleClass().add("list-cell-connected");
            }
            if (ping == null) {
                // Offline
                this.playersField.setText("");
                this.versionField.setText(LocaleManager.translate(Strings.OFFLINE));
                this.versionField.getStyleClass().add("version-error");
                setErrorMotd(String.format("%s", server.getLastPing().getLastConnectionException()));
                this.optionsConnect.setDisable(true);
                this.canConnect = false;
                return;
            }
            this.playersField.setText(LocaleManager.translate(Strings.SERVER_INFO_SLOTS_PLAYERS_ONLINE, ping.getPlayerOnline(), ping.getMaxPlayers()));
            Version serverVersion;
            if (server.getDesiredVersionId() == ProtocolDefinition.QUERY_PROTOCOL_VERSION_ID) {
                serverVersion = Versions.getVersionByProtocolId(ping.getProtocolId());
            } else {
                serverVersion = Versions.getVersionById(server.getDesiredVersionId());
                this.versionField.setStyle("-fx-text-fill: -secondary-light-light-color;");
            }
            if (serverVersion == null) {
                this.versionField.setText(ping.getServerBrand());
                this.versionField.setStyle("-fx-text-fill: red;");
                this.optionsConnect.setDisable(true);
                this.canConnect = false;
            } else {
                this.versionField.setText(serverVersion.getVersionName());
                this.optionsConnect.setDisable(false);
                this.canConnect = true;
            }
            this.brandField.setText(ping.getServerModInfo().getBrand());
            this.brandField.setTooltip(new Tooltip(ping.getServerModInfo().getInfo()));
            this.motdField.getChildren().setAll(ping.getMotd().getJavaFXText());
            if (ping.getFavicon() != null) {
                this.faviconField.setImage(GUITools.getImage(ping.getFavicon()));
                if (!Arrays.equals(ping.getFavicon(), server.getFavicon())) {
                    server.setFavicon(ping.getFavicon());
                    server.saveToConfig();
                }
            }
            if (server.isReadOnly()) {
                this.optionsEdit.setDisable(true);
                this.optionsDelete.setDisable(true);
            }
            if (server.getLastPing().getLastConnectionException() != null) {
                // connection failed because of an error in minosoft, but ping was okay
                this.versionField.setStyle("-fx-text-fill: red;");
                this.optionsConnect.setDisable(true);
                this.canConnect = false;
                setErrorMotd(String.format("%s: %s", server.getLastPing().getLastConnectionException().getClass().getCanonicalName(), server.getLastPing().getLastConnectionException().getMessage()));
            }
        })));
        server.getLastPing().registerEvent(new EventInvokerCallback<ServerListPongEvent>(ServerListPongEvent.class, event -> Platform.runLater(() -> {
            this.pingField.setText(String.format("%dms", event.getLatency()));
            switch (PingBars.byPing(event.getLatency())) {
                case BARS_5 -> this.pingField.getStyleClass().add("ping-5-bars");
                case BARS_4 -> this.pingField.getStyleClass().add("ping-4-bars");
                case BARS_3 -> this.pingField.getStyleClass().add("ping-3-bars");
                case BARS_2 -> this.pingField.getStyleClass().add("ping-2-bars");
                case BARS_1 -> this.pingField.getStyleClass().add("ping-1-bars");
                case NO_CONNECTION -> this.pingField.getStyleClass().add("ping-no-connection");
            }
        })));
    }

    public void setName(BaseComponent name) {
        this.nameField.getChildren().setAll(name.getJavaFXText());
        for (Node node : this.nameField.getChildren()) {
            node.setStyle("-fx-font-size: 15pt ;");
        }
    }

    private void resetCell() {
        // clear all cells
        setStyle(null);
        getStyleClass().remove("list-cell-connected");
        this.motdField.getChildren().clear();
        this.brandField.setText("");
        this.brandField.setTooltip(null);
        this.motdField.setStyle(null);
        this.versionField.setText(LocaleManager.translate(Strings.CONNECTING));
        this.versionField.getStyleClass().remove("version-error");
        this.versionField.setStyle(null);
        this.playersField.setText("");
        this.pingField.setText("");
        this.pingField.getStyleClass().removeIf((s -> s.startsWith("ping")));
        this.optionsConnect.setDisable(true);
        this.optionsEdit.setDisable(false);
        this.optionsDelete.setDisable(false);
    }

    private void setErrorMotd(String message) {
        Text text = new Text(message);
        text.setFill(Color.RED);
        this.motdField.getChildren().setAll(text);
    }

    public void delete() {
        if (this.server.isReadOnly()) {
            return;
        }
        this.server.getConnections().forEach(Connection::disconnect);
        this.server.delete();
        Log.info(String.format("Deleted server (name=\"%s\", address=\"%s\")", this.server.getName().getLegacyText(), this.server.getAddress()));
        SERVER_LIST_VIEW.getItems().remove(this.server);
    }

    public void refresh() {
        if (this.server.getLastPing() == null) {
            // server was not pinged, don't even try, only costs memory and cpu
            return;
        }
        Log.info(String.format("Refreshing server status (serverName=\"%s\", address=\"%s\")", this.server.getName().getLegacyText(), this.server.getAddress()));
        resetCell();
        this.server.ping();
    }

    public void clicked(MouseEvent e) {
        switch (e.getButton()) {
            case PRIMARY -> {
                if (e.getClickCount() == 2) {
                    connect();
                }
            }
            case SECONDARY -> this.optionsMenu.fire();
            case MIDDLE -> {
                SERVER_LIST_VIEW.getSelectionModel().select(this.server);
                editServer();
            }
        }
    }

    public void connect() {
        if (!this.canConnect || this.server.getLastPing() == null) {
            return;
        }
        Connection connection = new Connection(Connection.lastConnectionId++, this.server.getAddress(), new Player(Minosoft.getConfig().getSelectedAccount()));
        Version version;
        if (this.server.getDesiredVersionId() == ProtocolDefinition.QUERY_PROTOCOL_VERSION_ID) {
            version = this.server.getLastPing().getVersion();
        } else {
            version = Versions.getVersionById(this.server.getDesiredVersionId());
        }
        this.optionsConnect.setDisable(true);
        connection.connect(this.server.getLastPing().getAddress(), version);
        connection.registerEvent(new EventInvokerCallback<>(ConnectionStateChangeEvent.class, this::handleConnectionCallback));
        this.server.addConnection(connection);

    }

    public void editServer() {
        MainWindow.addOrEditServer(this.server);
    }

    private void handleConnectionCallback(ConnectionStateChangeEvent event) {
        Connection connection = event.getConnection();
        if (!this.server.getConnections().contains(connection)) {
            // the card got recycled
            return;
        }
        Platform.runLater(() -> {
            if (!connection.isConnected()) {
                // maybe we got disconnected
                if (!this.server.isConnected()) {
                    setStyle(null);
                    getStyleClass().remove("list-cell-connected");
                    this.optionsSessions.setDisable(true);
                    this.optionsConnect.setDisable(false);
                    return;
                }
            }

            this.optionsConnect.setDisable(Minosoft.getConfig().getSelectedAccount() == connection.getPlayer().getAccount());
            getStyleClass().add("list-cell-connected");
            this.optionsSessions.setDisable(false);
        });
    }

    public void showInfo() {
        JFXAlert<?> dialog = new JFXAlert<>();
        dialog.setTitle("View server info: " + this.server.getName().getMessage());
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
        serverNameLabel.getChildren().setAll(this.server.getName().getJavaFXText());
        Label serverAddressLabel = new Label(this.server.getAddress());
        Label forcedVersionLabel = new Label();

        if (this.server.getDesiredVersionId() == ProtocolDefinition.QUERY_PROTOCOL_VERSION_ID) {
            forcedVersionLabel.setText(Versions.LOWEST_VERSION_SUPPORTED.getVersionName());
        } else {
            forcedVersionLabel.setText(Versions.getVersionById(this.server.getDesiredVersionId()).getVersionName());
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

        if (this.server.getLastPing() != null) {
            if (this.server.getLastPing().getLastConnectionException() != null) {
                Label lastConnectionExceptionLabel = new Label(this.server.getLastPing().getLastConnectionException().toString());
                lastConnectionExceptionLabel.setStyle("-fx-text-fill: red");
                grid.add(new Label(LocaleManager.translate(Strings.SERVER_INFO_LAST_CONNECTION_EXCEPTION) + ":"), 0, ++column);
                grid.add(lastConnectionExceptionLabel, 1, column);
            }

            if (this.server.getLastPing().getLastPing() != null) {
                ServerListPing lastPing = this.server.getLastPing().getLastPing();
                Version serverVersion = Versions.getVersionByProtocolId(lastPing.getProtocolId());
                String serverVersionString;
                if (serverVersion == null) {
                    serverVersionString = LocaleManager.translate(Strings.SERVER_INFO_VERSION_UNKNOWN, lastPing.getProtocolId());
                } else {
                    serverVersionString = serverVersion.getVersionName();
                }
                Label realServerAddressLabel = new Label(this.server.getLastPing().getAddress().toString());
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
            SessionsWindow sessionsWindow = GUITools.showPane("/layout/dialogs/login_mojang.fxml", Modality.APPLICATION_MODAL, LocaleManager.translate(Strings.SESSIONS_DIALOG_TITLE, this.server.getName()));
            sessionsWindow.setServer(this.server);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
