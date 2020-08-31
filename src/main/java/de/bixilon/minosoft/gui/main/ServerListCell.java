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
import de.bixilon.minosoft.protocol.protocol.ConnectionReasons;
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
    public MenuItem optionsEdit;
    @FXML
    public MenuItem optionsDelete;
    boolean canConnect = false;
    Connection lastPing;
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

    /**
     * Initializes the controller class.
     */
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
        getRoot().getChildrenUnmodifiable().forEach(c -> c.setVisible(!empty));
        if (!empty && server != null && !server.equals(this.server)) {
            this.server = server;
            serverName.setText(server.getName());
            Image favicon = server.getFavicon();
            if (favicon == null) {
                favicon = GUITools.logo;
            }
            icon.setImage(favicon);
            optionsConnect.setOnAction(e -> connect());
            optionsEdit.setOnAction(e -> edit());
            optionsDelete.setOnAction(e -> delete());

            lastPing = new Connection(Connection.lastConnectionId++, server.getAddress(), null);
            lastPing.addPingCallback(ping -> Platform.runLater(() -> {
                if (ping == null) {
                    // Offline
                    players.setText("");
                    version.setText("Offline");
                    motd.setText("Could not connect to server!");
                    motd.setTextFill(Color.RED);
                    optionsConnect.setDisable(true);
                    canConnect = false;
                    return;
                }
                players.setText(String.format("%d/%d", ping.getPlayerOnline(), ping.getMaxPlayers()));
                Version serverVersion;
                if (server.getDesiredVersion() == -1) {
                    serverVersion = Versions.getVersionById(ping.getProtocolNumber());
                } else {
                    serverVersion = Versions.getVersionById(server.getDesiredVersion());
                }
                if (serverVersion == null) {
                    version.setText(ping.getServerVersion());
                    version.setTextFill(Color.RED);
                    optionsConnect.setDisable(true);
                    canConnect = false;
                } else {
                    version.setText(serverVersion.getVersionName());
                    canConnect = true;
                }
                motd.setText(ping.getMotd().getRawMessage());
                if (ping.getFavicon() != null) {
                    server.setBase64Favicon(ping.getBase64EncodedFavicon());
                    server.saveToConfig();
                    icon.setImage(ping.getFavicon());
                }
            }));
            lastPing.resolve(ConnectionReasons.PING, server.getDesiredVersion()); // resolve dns address and ping
        }
        setOnMouseClicked(click -> {
            if (click.getClickCount() == 2) {
                connect();
            }
        });
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
                ServerListCell.this.serverName.setText(serverName.getText());
                ServerListCell.this.server.setName(serverName.getText());
                ServerListCell.this.server.setDesiredVersion(GUITools.versionList.getSelectionModel().getSelectedItem().getProtocolVersion());
                ServerListCell.this.server.setAddress(DNSUtil.correctHostName(serverAddress.getText()));
                ServerListCell.this.server.saveToConfig();
                Log.info(String.format("Edited and saved server (serverName=%s, serverAddress=%s, version=%d)", server.getName(), server.getAddress(), server.getDesiredVersion()));
            }
            return null;
        });

        dialog.showAndWait();
    }

    public void delete() {
        listView.getItems().remove(server);
        server.delete();
    }

    public void connect() {
        if (!canConnect || lastPing == null) {
            return;
        }
        Connection connection = new Connection(Connection.lastConnectionId++, server.getAddress(), new Player(Minosoft.accountList.get(0)));
        Version version;
        if (server.getDesiredVersion() == -1) {
            version = lastPing.getVersion();
        } else {
            version = Versions.getVersionById(server.getDesiredVersion());
        }
        connection.connect(lastPing.getAddress(), version);
        setStyle("-fx-background-color: darkseagreen;");
    }
}