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
import de.bixilon.minosoft.game.datatypes.Player;
import de.bixilon.minosoft.game.datatypes.objectLoader.versions.Version;
import de.bixilon.minosoft.game.datatypes.objectLoader.versions.Versions;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.ping.ForgeModInfo;
import de.bixilon.minosoft.ping.ServerListPing;
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.util.DNSUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ServerListCell extends ListCell<Server> implements Initializable {
    public static ListView<Server> listView = new ListView<>();
    @FXML
    public ImageView icon;
    @FXML
    public Label motd;
    @FXML
    public Label version;
    @FXML
    public ImageView ping; //ToDo
    @FXML
    public Label players;
    @FXML
    public MenuItem optionsConnect;
    @FXML
    public MenuButton optionsMenu;
    @FXML
    public Label serverBrand;
    @FXML
    public MenuItem optionsSessions;
    boolean canConnect = false;
    @FXML
    private Label serverName;
    @FXML
    private AnchorPane root;
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

        Image favicon = server.getFavicon();
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
                version.setText("Offline");
                version.setStyle("-fx-text-fill: red;");
                motd.setText(String.format("%s", server.getLastPing().getLastConnectionException()));
                motd.setStyle("-fx-text-fill: red;");
                optionsConnect.setDisable(true);
                canConnect = false;
                return;
            }
            players.setText(String.format("%d/%d", ping.getPlayerOnline(), ping.getMaxPlayers()));
            Version serverVersion;
            if (server.getDesiredVersion() == -1) {
                serverVersion = Versions.getVersionById(ping.getProtocolId());
            } else {
                serverVersion = Versions.getVersionById(server.getDesiredVersion());
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
            motd.setText(ping.getMotd().getRawMessage());
            if (ping.getFavicon() != null) {
                icon.setImage(ping.getFavicon());
                if (!ping.getBase64EncodedFavicon().equals(server.getBase64Favicon())) {
                    server.setBase64Favicon(ping.getBase64EncodedFavicon());
                    server.saveToConfig();
                }
            }
            if (server.getLastPing().getLastConnectionException() != null) {
                // connection failed because of an error in minosoft, but ping was okay
                version.setStyle("-fx-text-fill: red;");
                optionsConnect.setDisable(true);
                canConnect = false;
                motd.setText(String.format("%s", server.getLastPing().getLastConnectionException().getLocalizedMessage()));
                motd.setStyle("-fx-text-fill: red;");
            }
        }));

    }

    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(selected);
    }

    public void edit() {
        Dialog<Object> dialog = new Dialog<>();
        dialog.setTitle("Edit server: " + server.getName());
        dialog.setHeaderText("Edit the details of the server");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 300, 10, 10));

        TextField serverName = new TextField();
        serverName.setPromptText("Servername");
        serverName.setText(server.getName());
        TextField serverAddress = new TextField();
        serverAddress.setPromptText("Server address");
        serverAddress.setText(server.getAddress());


        if (server.getDesiredVersion() == -1) {
            GUITools.versionList.getSelectionModel().select(Versions.getLowestVersionSupported());
        } else {
            GUITools.versionList.getSelectionModel().select(Versions.getVersionById(server.getDesiredVersion()));
        }

        grid.add(new Label("Servername:"), 0, 0);
        grid.add(serverName, 1, 0);
        grid.add(new Label("Server address:"), 0, 1);
        grid.add(serverAddress, 1, 1);
        grid.add(new Label("Version:"), 0, 2);
        grid.add(GUITools.versionList, 1, 2);

        Node saveButton = dialog.getDialogPane().lookupButton(saveButtonType);

        serverAddress.textProperty().addListener((observable, oldValue, newValue) -> saveButton.setDisable(newValue.trim().isEmpty()));

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(serverName::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                serverName.setText(serverName.getText());
                server.setName(serverName.getText());
                server.setDesiredVersion(GUITools.versionList.getSelectionModel().getSelectedItem().getProtocolVersion());
                if (server.getDesiredVersion() != -1) {
                    version.setText(Versions.getVersionById(server.getDesiredVersion()).getVersionName());
                    version.setTextFill(Color.BLACK);
                }
                server.setAddress(DNSUtil.correctHostName(serverAddress.getText()));
                server.saveToConfig();
                Log.info(String.format("Edited and saved server (serverName=%s, serverAddress=%s, version=%d)", server.getName(), server.getAddress(), server.getDesiredVersion()));
            }
            return null;
        });

        dialog.showAndWait();
    }

    public void delete() {
        server.delete();
        Log.info(String.format("Deleted server (name=\"%s\", address=\"%s\")", server.getName(), server.getAddress()));
        listView.getItems().remove(server);
    }

    public void connect() {
        if (!canConnect || server.getLastPing() == null) {
            return;
        }
        Connection connection = new Connection(Connection.lastConnectionId++, server.getAddress(), new Player(Minosoft.getSelectedAccount()));
        Version version;
        if (server.getDesiredVersion() == -1) {
            version = server.getLastPing().getVersion();
        } else {
            version = Versions.getVersionById(server.getDesiredVersion());
        }
        optionsConnect.setDisable(true);
        connection.connect(server.getLastPing().getAddress(), version);
        connection.addConnectionChangeCallback(this::handleConnectionCallback);
        server.addConnection(connection);

    }


    private void resetCell() {
        // clear all cells
        setStyle(null);
        motd.setText("");
        serverBrand.setText("");
        serverBrand.setTooltip(null);
        motd.setStyle(null);
        version.setText("Connecting...");
        version.setStyle(null);
        players.setText("");
        optionsConnect.setDisable(true);
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

    public void showInfo() {

        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("View server info: " + server.getName());

        ButtonType loginButtonType = ButtonType.CLOSE;
        dialog.getDialogPane().getButtonTypes().add(loginButtonType);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 300, 10, 10));

        Label serverNameLabel = new Label(server.getName());
        Label serverAddressLabel = new Label(server.getAddress());
        Label forcedVersionLabel = new Label();


        if (server.getDesiredVersion() == -1) {
            forcedVersionLabel.setText(Versions.getLowestVersionSupported().getVersionName());
        } else {
            forcedVersionLabel.setText(Versions.getVersionById(server.getDesiredVersion()).getVersionName());
        }

        int column = -1;
        grid.add(new Label("Servername:"), 0, ++column);
        grid.add(serverNameLabel, 1, column);
        grid.add(new Label("Server address:"), 0, ++column);
        grid.add(serverAddressLabel, 1, column);
        grid.add(new Label("Forced version:"), 0, ++column);
        grid.add(forcedVersionLabel, 1, column);

        if (server.getLastPing() != null) {
            if (server.getLastPing().getLastConnectionException() != null) {
                Label lastConnectionExceptionLabel = new Label(server.getLastPing().getLastConnectionException().toString());
                lastConnectionExceptionLabel.setStyle("-fx-text-fill: red");
                grid.add(new Label("Last connection exception:"), 0, ++column);
                grid.add(lastConnectionExceptionLabel, 1, column);
            }

            if (server.getLastPing().getLastPing() != null) {
                ServerListPing lastPing = server.getLastPing().getLastPing();
                Version serverVersion = Versions.getVersionById(lastPing.getProtocolId());
                String serverVersionString;
                if (serverVersion == null) {
                    serverVersionString = String.format("Unknown (%d)", lastPing.getProtocolId());
                } else {
                    serverVersionString = serverVersion.getVersionName();
                }
                Label realServerAddressLabel = new Label(server.getLastPing().getAddress().toString());
                Label serverVersionLabel = new Label(serverVersionString);
                Label serverBrandLabel = new Label(lastPing.getServerBrand());
                Label playersOnlineMaxLabel = new Label(String.format("%d/%d", lastPing.getPlayerOnline(), lastPing.getMaxPlayers()));
                Label motdLabel = new Label(lastPing.getMotd().getRawMessage());
                Label moddedBrandLabel = new Label(lastPing.getServerModInfo().getBrand());


                grid.add(new Label("Real server address:"), 0, ++column);
                grid.add(realServerAddressLabel, 1, column);
                grid.add(new Label("Server version:"), 0, ++column);
                grid.add(serverVersionLabel, 1, column);
                grid.add(new Label("Server brand:"), 0, ++column);
                grid.add(serverBrandLabel, 1, column);
                grid.add(new Label("Players online:"), 0, ++column);
                grid.add(playersOnlineMaxLabel, 1, column);
                grid.add(new Label("MotD:"), 0, ++column);
                grid.add(motdLabel, 1, column);
                grid.add(new Label("Modded brand:"), 0, ++column);
                grid.add(moddedBrandLabel, 1, column);

                if (lastPing.getServerModInfo() instanceof ForgeModInfo) {
                    ForgeModInfo modInfo = (ForgeModInfo) lastPing.getServerModInfo();
                    Label moddedModsLabel = new Label(modInfo.getModList().toString());
                    moddedModsLabel.setWrapText(true);

                    grid.add(new Label("Mod list:"), 0, ++column);
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
            stage.setTitle(String.format("Sessions - %s - Minosoft", server.getName()));
            stage.setScene(new Scene(parent));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}