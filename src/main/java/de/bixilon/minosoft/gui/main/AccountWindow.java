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
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.util.mojang.api.MojangAccount;
import de.bixilon.minosoft.util.mojang.api.MojangAccountAuthenticationAttempt;
import de.bixilon.minosoft.util.mojang.api.MojangAuthentication;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import java.net.URL;
import java.util.ResourceBundle;

public class AccountWindow implements Initializable {
    @FXML
    public BorderPane accountPane;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        AccountListCell.listView.setCellFactory((lv) -> AccountListCell.newInstance());

        ObservableList<MojangAccount> accounts = FXCollections.observableArrayList();
        accounts.addAll(Minosoft.getAccountList().values());
        AccountListCell.listView.setItems(accounts);
        accountPane.setCenter(AccountListCell.listView);
    }

    @FXML
    public void addAccount() {
        Dialog<Object> dialog = new Dialog<>();
        dialog.setTitle("Add account");

        ButtonType loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();

        TextField email = new TextField();
        email.setPromptText("Email");
        PasswordField password = new PasswordField();
        password.setPromptText("Password");


        grid.add(new Label("Email:"), 0, 0);
        grid.add(email, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(password, 1, 1);
        Node loginButton = dialog.getDialogPane().lookupButton(loginButtonType);

        email.textProperty().addListener((observable, oldValue, newValue) -> loginButton.setDisable(newValue.trim().isEmpty()));
        loginButton.setDisable(true);

        dialog.getDialogPane().setContent(grid);

        Platform.runLater(email::requestFocus);
        loginButton.addEventFilter(ActionEvent.ACTION, event -> {
                    MojangAccountAuthenticationAttempt attempt = MojangAuthentication.login(email.getText(), password.getText());
                    if (attempt.succeeded()) {
                        // login okay
                        MojangAccount account = attempt.getAccount();
                        Minosoft.accountList.put(account.getUserId(), account);
                        account.saveToConfig();
                        AccountListCell.listView.getItems().add(account);
                        Log.info(String.format("Added and saved account (playerName=%s, email=%s, uuid=%s)", account.getPlayerName(), account.getMojangUserName(), account.getUUID()));
                        return;
                    }
                    event.consume();
                    Label error = new Label();
                    error.setStyle("-fx-text-fill: red");
                    error.setText(attempt.getError());

                    grid.add(new Label("Error:"), 0, 2);
                    grid.add(error, 1, 2);
                    // ToDo resize window
                }
        );

        Window window = dialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(windowEvent -> window.hide());


        dialog.showAndWait();
    }
}
