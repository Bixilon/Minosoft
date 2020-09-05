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
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainWindow implements Initializable {
    @FXML
    public BorderPane serversPane;
    @FXML
    public Menu accountMenu;

    public static void manageAccounts() {
        try {
            Parent parent = FXMLLoader.load(MainWindow.class.getResource("/layout/accounts.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Manage accounts - Minosoft");
            stage.setScene(new Scene(parent));
            Platform.setImplicitExit(false);
            stage.setOnCloseRequest(event -> {
                if (Minosoft.getSelectedAccount() == null) {
                    event.consume();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Please select an account!");
                    alert.setContentText("You did not select an account. Minosoft does not know which account you want to use to connect to a server!");
                    alert.showAndWait();
                } else {
                    stage.close();
                }
            });
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        serversPane.setCenter(ServerListCell.listView);
        if (Minosoft.getSelectedAccount() != null) {
            accountMenu.setText(String.format("Account (%s)", Minosoft.getSelectedAccount().getPlayerName()));
        }
    }

    @FXML
    public void addServer() {
        Dialog<Object> dialog = new Dialog<>();
        dialog.setTitle("Add server");
        dialog.setHeaderText("Enter the details of the server");

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 300, 10, 10));

        TextField serverName = new TextField();
        serverName.setPromptText("Servername");
        serverName.setText("A Minosoft server");
        TextField serverAddress = new TextField();
        serverAddress.setPromptText("Server address");

        GUITools.versionList.getSelectionModel().select(Versions.getLowestVersionSupported());

        grid.add(new Label("Servername:"), 0, 0);
        grid.add(serverName, 1, 0);
        grid.add(new Label("Server address:"), 0, 1);
        grid.add(serverAddress, 1, 1);
        grid.add(new Label("Version:"), 0, 2);
        grid.add(GUITools.versionList, 1, 2);

        Node addButton = dialog.getDialogPane().lookupButton(addButtonType);

        serverAddress.textProperty().addListener((observable, oldValue, newValue) -> addButton.setDisable(newValue.trim().isEmpty()));
        addButton.setDisable(true);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(serverName::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                Server server = new Server(Server.getNextServerId(), serverName.getText(), DNSUtil.correctHostName(serverAddress.getText()), GUITools.versionList.getSelectionModel().getSelectedItem().getProtocolVersion());
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

    public void refreshServers() {
        Log.info("Refreshing server list");
        for (Server server : ServerListCell.listView.getItems()) {
            if (server.getLastPing() == null) {
                // server was not pinged, don't even try, only costs memory and cpu
                continue;
            }
            server.ping();
            ServerListCell.listView.refresh();
        }
    }

    public void manageAccounts(ActionEvent actionEvent) {
        manageAccounts();
    }
}
