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
import de.bixilon.minosoft.game.datatypes.objectLoader.versions.Versions;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.util.DNSUtil;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.util.Pair;

import java.net.URL;
import java.util.ResourceBundle;

public class MainWindow implements Initializable {
    @FXML
    public BorderPane serversPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serversPane.setCenter(ServerListCell.listView);
    }

    @FXML
    public void addServer() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Add server");
        dialog.setHeaderText("Enter the details of the server");

        ButtonType loginButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 300, 10, 10));

        TextField serverName = new TextField();
        serverName.setPromptText("Servername");
        TextField serverAddress = new TextField();
        serverAddress.setPromptText("Server address");

        GUITools.versionList.getSelectionModel().select(Versions.getLowestVersionSupported());

        grid.add(new Label("Servername:"), 0, 0);
        grid.add(serverName, 1, 0);
        grid.add(new Label("Server address:"), 0, 1);
        grid.add(serverAddress, 1, 1);
        grid.add(new Label("Version:"), 0, 2);
        grid.add(GUITools.versionList, 1, 2);

        Node addButton = dialog.getDialogPane().lookupButton(loginButtonType);

        serverAddress.textProperty().addListener((observable, oldValue, newValue) -> addButton.setDisable(newValue.trim().isEmpty()));
        addButton.setDisable(true);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(serverName::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                Server server = new Server(Minosoft.serverList.size() + 1, serverName.getText(), DNSUtil.correctHostName(serverAddress.getText()), null, GUITools.versionList.getSelectionModel().getSelectedItem().getProtocolVersion());
                Minosoft.serverList.add(server);
                server.saveToConfig();
                ServerListCell.listView.getItems().add(server);
                Log.info(String.format("Added and saved server (serverName=%s, serverAddress=%s, version=%d)", server.getName(), server.getAddress(), server.getDesiredVersion()));
            }
            return null;
        });

        dialog.showAndWait();
    }

    @FXML
    public void quit() {
        System.exit(0);
    }
}
