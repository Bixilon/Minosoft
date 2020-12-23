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

package de.bixilon.minosoft.gui.main.dialogs;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.data.accounts.MojangAccount;
import de.bixilon.minosoft.data.locale.LocaleManager;
import de.bixilon.minosoft.data.locale.Strings;
import de.bixilon.minosoft.gui.main.cells.AccountListCell;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.util.mojang.api.MojangAuthentication;
import de.bixilon.minosoft.util.mojang.api.exceptions.AuthenticationException;
import de.bixilon.minosoft.util.mojang.api.exceptions.NoNetworkConnectionException;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MojangLoginController implements Initializable {
    public HBox hBox;
    public Label header;
    public Label emailLabel;
    public JFXTextField email;
    public Label passwordLabel;
    public JFXPasswordField password;
    public JFXButton loginButton;
    public Label errorMessage;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // translate
        this.header.setText(LocaleManager.translate(Strings.LOGIN_MOJANG_DIALOG_HEADER));
        this.emailLabel.setText(LocaleManager.translate(Strings.EMAIL));
        this.passwordLabel.setText(LocaleManager.translate(Strings.PASSWORD));
        this.loginButton.setText(LocaleManager.translate(Strings.BUTTON_LOGIN));


        this.email.textProperty().addListener(this::checkData);
        this.password.textProperty().addListener(this::checkData);


        this.hBox.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                if (this.loginButton.isDisable()) {
                    return;
                }
                this.loginButton.fire();
                return;
            }
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                close();
            }
        });
    }

    public void login(ActionEvent event) {
        event.consume();
        this.email.setDisable(true);
        this.password.setDisable(true);
        this.loginButton.setDisable(true);
        this.errorMessage.setVisible(false);


        new Thread(() -> { // ToDo: recycle thread
            try {
                MojangAccount account = MojangAuthentication.login(this.email.getText(), this.password.getText());

                account.setNeedRefresh(false);
                Minosoft.getConfig().putAccount(account);
                account.saveToConfig();
                Log.info(String.format("Added and saved account (type=mojang, username=%s, email=%s, uuid=%s)", account.getUsername(), account.getEmail(), account.getUUID()));
                Platform.runLater(() -> {
                    AccountListCell.ACCOUNT_LIST_VIEW.getItems().add(account);
                    close();
                });
                if (Minosoft.getConfig().getSelectedAccount() == null) {
                    // select account
                    Minosoft.selectAccount(account);
                }
            } catch (AuthenticationException | NoNetworkConnectionException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    this.errorMessage.setText(e.getMessage());
                    this.errorMessage.setVisible(true);
                    this.email.setDisable(false);
                    this.password.setDisable(false);
                    this.loginButton.setDisable(true);
                });
            }
        }, "AccountLoginThread").start();

    }

    private void checkData(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
        if (newValue.isEmpty()) {
            this.loginButton.setDisable(true);
            return;
        }
        this.loginButton.setDisable(this.email.getText().isBlank() || this.password.getText().isBlank());
    }

    public void close() {
        getStage().close();
    }

    public Stage getStage() {
        return ((Stage) this.hBox.getScene().getWindow());
    }
}
