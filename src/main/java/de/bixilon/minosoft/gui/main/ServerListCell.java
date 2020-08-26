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
import javafx.util.Callback;
import javafx.util.Pair;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class ServerListCell extends ListCell<Server> implements Initializable {

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
            optionsConnect.setOnAction(e -> {
                Connection connection = new Connection(Connection.lastConnectionId++, server.getAddress(), new Player(Minosoft.accountList.get(0)));
                connection.resolve(ConnectionReasons.CONNECT, server.getDesiredVersion());
            });
            optionsEdit.setOnAction(e -> edit());

            Connection connection = new Connection(Connection.lastConnectionId++, server.getAddress(), null);
            connection.addPingCallback(ping -> Platform.runLater(() -> {
                if (ping == null) {
                    // Offline
                    players.setText("");
                    version.setText("Offline");
                    optionsConnect.setDisable(true);
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
                } else {
                    version.setText(serverVersion.getVersionName());
                }
                motd.setText(ping.getMotd().getRawMessage());
                if (ping.getFavicon() != null) {
                    server.setBase64Favicon(ping.getBase64EncodedFavicon());
                    server.saveToConfig();
                    icon.setImage(ping.getFavicon());
                }
            }));
            connection.resolve(ConnectionReasons.PING); // resolve dns address and ping
        }
    }

    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(selected);
    }

    public void edit() {
        // Create the custom dialog.
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Edit server: " + server.getName());
        dialog.setHeaderText("Edit the details of the server");


// Set the button types.
        ButtonType loginButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

// Create the username and password labels and fields.
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

        ComboBox<Version> versionList = new ComboBox<>(GUITools.versions);
        versionList.setCellFactory(new Callback<>() {
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
        if (server.getDesiredVersion() == -1) {
            versionList.getSelectionModel().select(Versions.getLowestVersionSupported());
        } else {
            versionList.getSelectionModel().select(Versions.getVersionById(server.getDesiredVersion()));
        }

        grid.add(new Label("Servername:"), 0, 0);
        grid.add(serverName, 1, 0);
        grid.add(new Label("Server address:"), 0, 1);
        grid.add(serverAddress, 1, 1);
        grid.add(new Label("Version:"), 0, 2);
        grid.add(versionList, 1, 2);

// Enable/Disable login button depending on whether a username was entered.
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);

// Do some validation (using the Java 8 lambda syntax).
        serverAddress.textProperty().addListener((observable, oldValue, newValue) -> {
            loginButton.setDisable(newValue.trim().isEmpty());
        });

        dialog.getDialogPane().setContent(grid);

// Request focus on the username field by default.
        Platform.runLater(serverName::requestFocus);

// Convert the result to a username-password-pair when the login button is clicked.
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                ServerListCell.this.serverName.setText(serverName.getText());
                ServerListCell.this.server.setName(serverName.getText());
                ServerListCell.this.server.setDesiredVersion(versionList.getSelectionModel().getSelectedItem().getProtocolVersion());
                ServerListCell.this.server.setAddress(DNSUtil.correctHostName(serverAddress.getText()));
                ServerListCell.this.server.saveToConfig();
                Log.info(String.format("Edited and saved server (serverName=%s, serverAddress=%s, version=%d)", server.getName(), server.getAddress(), server.getDesiredVersion()));
            }
            return null;
        });

        dialog.showAndWait();
    }
}