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
import de.bixilon.minosoft.protocol.network.Connection;
import de.bixilon.minosoft.protocol.protocol.ConnectionReasons;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;

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
    private Server model;

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
        if (!empty && server != null && !server.equals(this.model)) {
            serverName.setText(server.getName());
            Image favicon = server.getFavicon();
            if (favicon == null) {
                favicon = GUITools.logo;
            }
            icon.setImage(favicon);

            Connection connection = new Connection(server.getId(), server.getAddress(), new Player(Minosoft.accountList.get(0)));
            connection.addPingCallback(ping -> Platform.runLater(() -> {
                if (ping == null) {
                    // Offline
                    players.setText("");
                    version.setText("Offline");
                    optionsConnect.setDisable(true);
                    return;
                }
                players.setText(String.format("%d/%d", ping.getPlayerOnline(), ping.getMaxPlayers()));
                Version serverVersion = Versions.getVersionById(ping.getProtocolNumber());
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
            connection.resolve(ConnectionReasons.PING); // resolve dns address and connect
        }
        this.model = server;
    }

    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(selected);
    }
}