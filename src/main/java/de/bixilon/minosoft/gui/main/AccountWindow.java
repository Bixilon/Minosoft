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

import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.ShutdownReasons;
import de.bixilon.minosoft.data.accounts.Account;
import de.bixilon.minosoft.data.locale.LocaleManager;
import de.bixilon.minosoft.data.locale.Strings;
import de.bixilon.minosoft.gui.main.cells.AccountListCell;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AccountWindow implements Initializable {
    public BorderPane accountPane;
    public MenuItem menuAddMojangAccount;
    public MenuItem menuAddOfflineAccount;
    public MenuItem menuAddMicrosoftAccount;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        AccountListCell.ACCOUNT_LIST_VIEW.setCellFactory((lv) -> AccountListCell.newInstance());

        ObservableList<Account> accounts = FXCollections.observableArrayList(Minosoft.getConfig().getConfig().getAccount().getEntries().values());
        AccountListCell.ACCOUNT_LIST_VIEW.setItems(accounts);
        this.accountPane.setCenter(AccountListCell.ACCOUNT_LIST_VIEW);

        this.menuAddMicrosoftAccount.setText(LocaleManager.translate(Strings.ACCOUNT_MODAL_MENU_ADD_MICROSOFT_ACCOUNT));
        this.menuAddMojangAccount.setText(LocaleManager.translate(Strings.ACCOUNT_MODAL_MENU_ADD_MOJANG_ACCOUNT));
        this.menuAddOfflineAccount.setText(LocaleManager.translate(Strings.ACCOUNT_MODAL_MENU_ADD_OFFLINE_ACCOUNT));
    }

    public void addMicrosoftAccount() {
        try {
            GUITools.showPane("/layout/dialogs/login/microsoft.fxml", Modality.APPLICATION_MODAL, LocaleManager.translate(Strings.LOGIN_MICROSOFT_DIALOG_TITLE));
        } catch (IOException e) {
            e.printStackTrace();
            Minosoft.shutdown(e.getMessage(), ShutdownReasons.LAUNCHER_FXML_LOAD_ERROR);
        }
    }

    public void addMojangAccount() {
        try {
            GUITools.showPane("/layout/dialogs/login/mojang.fxml", Modality.APPLICATION_MODAL, LocaleManager.translate(Strings.LOGIN_MOJANG_DIALOG_TITLE));
        } catch (IOException e) {
            e.printStackTrace();
            Minosoft.shutdown(e.getMessage(), ShutdownReasons.LAUNCHER_FXML_LOAD_ERROR);
        }
    }

    public void addOfflineAccount() {
        try {
            GUITools.showPane("/layout/dialogs/login/offline.fxml", Modality.APPLICATION_MODAL, LocaleManager.translate(Strings.LOGIN_OFFLINE_DIALOG_TITLE));
        } catch (IOException e) {
            e.printStackTrace();
            Minosoft.shutdown(e.getMessage(), ShutdownReasons.LAUNCHER_FXML_LOAD_ERROR);
        }
    }
}
