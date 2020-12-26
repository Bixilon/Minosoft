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

import de.bixilon.minosoft.data.locale.LocaleManager;
import de.bixilon.minosoft.data.locale.Strings;
import de.bixilon.minosoft.protocol.network.Connection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;

import java.net.URL;
import java.util.ResourceBundle;

public class SessionsWindow implements Initializable {
    public BorderPane accountPane;
    public Menu menuDisconnect;
    public MenuItem menuDisconnectFromAll;

    Server server;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        SessionListCell.CONNECTION_LIST_VIEW.setCellFactory((lv) -> SessionListCell.newInstance());

        this.menuDisconnect.setText(LocaleManager.translate(Strings.SESSIONS_MENU_DISCONNECT));
        this.menuDisconnectFromAll.setText(LocaleManager.translate(Strings.SESSIONS_MENU_DISCONNECT_FROM_ALL));
    }

    public void setServer(Server server) {
        this.server = server;
        ObservableList<Connection> connections = FXCollections.observableArrayList();
        for (Connection connection : server.getConnections()) {
            if (!connection.isConnected()) {
                server.getConnections().remove(connection);
            }
            connections.add(connection);
        }
        SessionListCell.CONNECTION_LIST_VIEW.setItems(connections);
        this.accountPane.setCenter(SessionListCell.CONNECTION_LIST_VIEW);
    }

    public void disconnectAll() {
        this.server.getConnections().forEach(Connection::disconnect);
    }
}
