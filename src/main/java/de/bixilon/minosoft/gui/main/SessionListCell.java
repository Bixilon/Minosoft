/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.main;

import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.data.locale.LocaleManager;
import de.bixilon.minosoft.data.locale.Strings;
import de.bixilon.minosoft.data.mappings.ResourceLocation;
import de.bixilon.minosoft.modding.event.CallbackEventInvoker;
import de.bixilon.minosoft.modding.event.events.ConnectionStateChangeEvent;
import de.bixilon.minosoft.protocol.network.connection.PlayConnection;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
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

public class SessionListCell extends ListCell<PlayConnection> implements Initializable {
    public static final ListView<PlayConnection> CONNECTION_LIST_VIEW = new ListView<>();

    public Label account;
    public Label connectionId;
    public MenuItem optionsDisconnect;
    public AnchorPane root;

    private PlayConnection connection;

    public static SessionListCell newInstance() {
        FXMLLoader loader = new FXMLLoader(Minosoft.MINOSOFT_ASSETS_MANAGER.getAssetURL(new ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "layout/cells/session.fxml")));
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

        this.optionsDisconnect.setText(LocaleManager.translate(Strings.SESSIONS_ACTION_DISCONNECT));
    }

    public AnchorPane getRoot() {
        return this.root;
    }

    @Override
    protected void updateItem(PlayConnection connection, boolean empty) {
        super.updateItem(connection, empty);

        this.root.setVisible(!empty);
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
        connection.registerEvent(new CallbackEventInvoker<>(this::handleConnectionCallback));
        this.connectionId.setText(String.format("#%d", connection.getConnectionId()));
        this.account.setText(connection.getAccount().getUsername());
    }

    private void handleConnectionCallback(ConnectionStateChangeEvent event) {
        PlayConnection connection = (PlayConnection) event.getConnection();
        if (this.connection != connection) {
            // the card got recycled
            return;
        }

        if (!connection.isConnected()) {
            Platform.runLater(() -> {
                CONNECTION_LIST_VIEW.getItems().remove(connection);
                if (CONNECTION_LIST_VIEW.getItems().isEmpty()) {
                    ((Stage) this.root.getScene().getWindow()).close();
                }
            });
        }
    }

    public void disconnect() {
        setStyle("-fx-background-color: indianred");
        this.connection.disconnect();
    }
}
