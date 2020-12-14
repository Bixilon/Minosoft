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

import com.jfoenix.controls.*;
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.data.locale.LocaleManager;
import de.bixilon.minosoft.data.locale.Strings;
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
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

import java.net.URL;
import java.util.ResourceBundle;

public class AccountWindow implements Initializable {

    public BorderPane accountPane;
    public MenuItem menuAddAccount;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        AccountListCell.MOJANG_ACCOUNT_LIST_VIEW.setCellFactory((lv) -> AccountListCell.newInstance());

        ObservableList<MojangAccount> accounts = FXCollections.observableArrayList(Minosoft.getConfig().getAccountList().values());
        AccountListCell.MOJANG_ACCOUNT_LIST_VIEW.setItems(accounts);
        this.accountPane.setCenter(AccountListCell.MOJANG_ACCOUNT_LIST_VIEW);

        this.menuAddAccount.setText(LocaleManager.translate(Strings.ACCOUNT_MODAL_MENU_ADD_ACCOUNT));
    }

    @FXML
    public void addAccount() {
        JFXAlert<?> dialog = new JFXAlert<>();
        dialog.setTitle(LocaleManager.translate(Strings.LOGIN_DIALOG_TITLE));
        GUITools.initializePane(dialog.getDialogPane());
        JFXDialogLayout layout = new JFXDialogLayout();
        layout.setHeading(new Label(LocaleManager.translate(Strings.LOGIN_DIALOG_HEADER)));

        JFXButton loginButton = new JFXButton(LocaleManager.translate(Strings.BUTTON_LOGIN));
        layout.setActions(loginButton);

        GridPane gridPane = new GridPane();
        gridPane.setHgap(15);
        gridPane.setVgap(15);

        JFXTextField emailField = new JFXTextField();
        emailField.setPromptText(LocaleManager.translate(Strings.EMAIL));

        JFXPasswordField passwordField = new JFXPasswordField();
        passwordField.setPromptText(LocaleManager.translate(Strings.PASSWORD));

        gridPane.add(new Label(LocaleManager.translate(Strings.EMAIL) + ":"), 0, 0);
        gridPane.add(emailField, 1, 0);
        gridPane.add(new Label(LocaleManager.translate(Strings.PASSWORD) + ":"), 0, 1);
        gridPane.add(passwordField, 1, 1);

        emailField.textProperty().addListener((observable, oldValue, newValue) -> loginButton.setDisable(newValue.trim().isEmpty()));
        loginButton.setDisable(true);

        layout.setBody(gridPane);
        dialog.setContent(layout);

        Platform.runLater(emailField::requestFocus);
        loginButton.addEventFilter(ActionEvent.ACTION, event -> {
            MojangAccountAuthenticationAttempt attempt = MojangAuthentication.login(emailField.getText(), passwordField.getText());
            if (attempt.succeeded()) {
                // login okay
                MojangAccount account = attempt.getAccount();
                Minosoft.getConfig().putMojangAccount(account);
                account.saveToConfig();
                AccountListCell.MOJANG_ACCOUNT_LIST_VIEW.getItems().add(account);
                Log.info(String.format("Added and saved account (playerName=%s, email=%s, uuid=%s)", account.getPlayerName(), account.getMojangUserName(), account.getUUID()));
                dialog.close();
                return;
            }
            event.consume();
            Label error = new Label();
            error.setStyle("-fx-text-fill: red");
            error.setText(attempt.getError());

            gridPane.add(new Label(LocaleManager.translate(Strings.ERROR)), 0, 2);
            gridPane.add(error, 1, 2);
            // ToDo resize window
        });

        Window window = dialog.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest(windowEvent -> window.hide());

        dialog.getDialogPane().setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() != KeyCode.ENTER) {
                return;
            }
            if (emailField.getText().trim().isEmpty()) {
                return;
            }
            loginButton.fire();
        });

        dialog.showAndWait();
    }
}
