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
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.util.DNSUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
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
        if (server.getLastPing() == null) {
            server.ping();
        }
        server.getLastPing().addPingCallback(ping -> Platform.runLater(() -> {
            if (server != this.server) {
                // cell does not contains us anymore
                return;
            }
            resetCell();
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
                if (ping.getBase64EncodedFavicon().equals(server.getBase64Favicon())) {
                    return;
                }
                server.setBase64Favicon(ping.getBase64EncodedFavicon());
                server.saveToConfig();
            }
        }));

    }

    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(selected);
    }

    public void edit() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Edit server: " + server.getName());
        dialog.setHeaderText("Edit the details of the server");

        ButtonType loginButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

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

        Node saveButton = dialog.getDialogPane().lookupButton(loginButtonType);

        serverAddress.textProperty().addListener((observable, oldValue, newValue) -> saveButton.setDisable(newValue.trim().isEmpty()));

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(serverName::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
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
        listView.getItems().remove(server);
        server.delete();
        Log.info(String.format("Deleted server (name=\"%s\", address=\"%s\")", server.getName(), server.getAddress()));
    }

    public void connect() {
        if (!canConnect || server.getLastPing() == null) {
            return;
        }
        Connection connection = new Connection(Connection.lastConnectionId++, server.getAddress(), new Player(Minosoft.accountList.get(0)));
        Version version;
        if (server.getDesiredVersion() == -1) {
            version = server.getLastPing().getVersion();
        } else {
            version = Versions.getVersionById(server.getDesiredVersion());
        }
        connection.connect(server.getLastPing().getAddress(), version);
        setStyle("-fx-background-color: darkseagreen;");
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
        Log.info(String.format("Refreshing server status (serverName=\"%s\", address=\"%s\"", server.getName(), server.getAddress()));
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
}