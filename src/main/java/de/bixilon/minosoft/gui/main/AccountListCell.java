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
import de.bixilon.minosoft.data.accounts.Account;
import de.bixilon.minosoft.data.locale.LocaleManager;
import de.bixilon.minosoft.data.locale.Strings;
import de.bixilon.minosoft.logging.Log;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AccountListCell extends ListCell<Account> implements Initializable {
    public static final ListView<Account> MOJANG_ACCOUNT_LIST_VIEW = new ListView<>();

    public MenuButton optionsMenu;
    public Label playerName;
    public MenuItem optionsSelect;
    public Label email;
    public MenuItem optionsDelete;
    public AnchorPane root;

    private Account account;

    public static AccountListCell newInstance() {
        FXMLLoader loader = new FXMLLoader(AccountListCell.class.getResource("/layout/cells/account.fxml"));
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

        // change locale
        this.optionsSelect.setText(LocaleManager.translate(Strings.ACCOUNTS_ACTION_SELECT));
        this.optionsDelete.setText(LocaleManager.translate(Strings.ACCOUNTS_ACTION_DELETE));

    }

    public AnchorPane getRoot() {
        return this.root;
    }

    @Override
    protected void updateItem(Account account, boolean empty) {
        super.updateItem(account, empty);

        this.root.setVisible(!empty);
        if (empty) {
            return;
        }

        if (account == null) {
            return;
        }

        if (account.equals(this.account)) {
            return;
        }
        resetCell();

        this.account = account;
        this.playerName.setText(account.getUsername());
        //  this.email.setText(account.getEmail());
        if (Minosoft.getConfig().getSelectedAccount() == account) {
            setStyle("-fx-background-color: darkseagreen;");
            this.optionsSelect.setDisable(true);
        }
    }

    private void resetCell() {
        // clear all cells
        setStyle(null);
        this.optionsSelect.setDisable(false);
    }

    public void logout() {
        this.account.logout();
        Minosoft.getConfig().removeAccount(this.account);
        Minosoft.getConfig().saveToFile();
        if (Minosoft.getConfig().getSelectedAccount() == this.account) {
            if (Minosoft.getConfig().getAccounts().isEmpty()) {
                Minosoft.selectAccount(null);
            } else {
                Minosoft.selectAccount(Minosoft.getConfig().getAccounts().values().iterator().next());
            }
            MOJANG_ACCOUNT_LIST_VIEW.refresh();
        }
        Log.info(String.format("Deleted account (id=%s, username=%s)", this.account.getId(), this.account.getUsername()));
        MOJANG_ACCOUNT_LIST_VIEW.getItems().remove(this.account);
    }

    public void clicked(MouseEvent e) {
        switch (e.getButton()) {
            case PRIMARY -> {
                if (e.getClickCount() == 2) {
                    select();
                }
            }
            case SECONDARY -> this.optionsMenu.fire();
        }
    }

    public void select() {
        Minosoft.selectAccount(this.account);
        MOJANG_ACCOUNT_LIST_VIEW.refresh();
        ServerListCell.SERVER_LIST_VIEW.refresh();
    }
}
