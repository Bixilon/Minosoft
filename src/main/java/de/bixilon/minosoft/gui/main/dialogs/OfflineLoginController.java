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
import com.jfoenix.controls.JFXTextField;
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.data.accounts.OfflineAccount;
import de.bixilon.minosoft.data.locale.LocaleManager;
import de.bixilon.minosoft.data.locale.Strings;
import de.bixilon.minosoft.gui.main.cells.AccountListCell;
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import de.bixilon.minosoft.util.Util;
import de.bixilon.minosoft.util.logging.Log;
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

public class OfflineLoginController implements Initializable {
    public HBox hBox;
    public Label header;
    public Label usernameLabel;
    public JFXTextField username;
    public Label uuidLabel;
    public JFXTextField uuid;
    public JFXButton addButton;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // translate
        this.header.setText(LocaleManager.translate(Strings.LOGIN_OFFLINE_DIALOG_HEADER));
        this.usernameLabel.setText(LocaleManager.translate(Strings.LOGIN_OFFLINE_USERNAME));
        this.uuidLabel.setText(LocaleManager.translate(Strings.LOGIN_OFFLINE_UUID));
        this.addButton.setText(LocaleManager.translate(Strings.LOGIN_OFFLINE_ADD_BUTTON));


        this.username.textProperty().addListener(this::checkData);
        this.uuid.textProperty().addListener(this::checkData);


        this.hBox.setOnKeyReleased(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                if (this.addButton.isDisable()) {
                    return;
                }
                this.addButton.fire();
                return;
            }
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                close();
            }
        });
    }

    public void add(ActionEvent event) {
        event.consume();
        OfflineAccount account;
        if (this.uuid.getText().isBlank()) {
            account = new OfflineAccount(this.username.getText());
        } else {
            account = new OfflineAccount(this.username.getText(), Util.getUUIDFromString(this.uuid.getText()));
        }

        Minosoft.getConfig().putAccount(account);
        account.saveToConfig();
        Log.info(String.format("Added and saved account (type=offline, username=%s, uuid=%s)", account.getUsername(), account.getUUID()));
        Platform.runLater(() -> {
            AccountListCell.ACCOUNT_LIST_VIEW.getItems().add(account);
            close();
        });
        if (Minosoft.getConfig().getSelectedAccount() == null) {
            // select account
            Minosoft.selectAccount(account);
        }

    }

    private void checkData(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
        if (!ProtocolDefinition.MINECRAFT_NAME_VALIDATOR.matcher(this.username.getText()).matches()) {
            this.addButton.setDisable(true);
            return;
        }
        if (!this.uuid.getText().isBlank()) {
            try {
                Util.getUUIDFromString(this.uuid.getText());
            } catch (IllegalArgumentException e) {
                this.addButton.setDisable(true);
                return;
            }
        }
        this.addButton.setDisable(false);
    }

    public void close() {
        getStage().close();
    }

    public Stage getStage() {
        return ((Stage) this.hBox.getScene().getWindow());
    }
}
