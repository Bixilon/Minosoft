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

import de.bixilon.minosoft.gui.LocaleManager;
import de.bixilon.minosoft.gui.Strings;
import de.bixilon.minosoft.protocol.network.Connection;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class SessionListCell extends ListCell<Connection> implements Initializable {
    public static final ListView<Connection> listView = new ListView<>();

    public Label account;
    public Label connectionId;
    public MenuItem optionsDisconnect;
    public AnchorPane root;

    private Connection connection;

    public static SessionListCell newInstance() {
        FXMLLoader loader = new FXMLLoader(SessionListCell.class.getResource("/layout/cells/session.fxml"));
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

        optionsDisconnect.setText(LocaleManager.translate(Strings.SESSIONS_ACTION_DISCONNECT));
    }

    public AnchorPane getRoot() {
        return root;
    }

    @Override
    protected void updateItem(Connection connection, boolean empty) {
        super.updateItem(connection, empty);

        root.setVisible(!empty);
        if (empty) {
            return;
        }

        if (connection == null) {
            return;
        }

        if (connection.equals(this.connection)) {
            return;
        }
        setStyle(null);
        this.connection = connection;
        connection.addConnectionChangeCallback(this::handleConnectionCallback);
        connectionId.setText(String.format("#%d", connection.getConnectionId()));
        account.setText(connection.getPlayer().getAccount().getPlayerName());
    }

    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(selected);
    }

    private void handleConnectionCallback(Connection connection) {
        if (this.connection != connection) {
            // the card got recycled
            return;
        }

        if (!connection.isConnected()) {
            Platform.runLater(() -> {
                listView.getItems().remove(connection);
                if (listView.getItems().size() == 0) {
                    ((Stage) root.getScene().getWindow()).close();
                }
            });
        }
    }

    public void disconnect() {
        setStyle("-fx-background-color: indianred");
        connection.disconnect();
    }
}