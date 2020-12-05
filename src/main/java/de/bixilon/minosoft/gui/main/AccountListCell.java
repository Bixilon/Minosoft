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
import de.bixilon.minosoft.data.locale.LocaleManager;
import de.bixilon.minosoft.data.locale.Strings;
import de.bixilon.minosoft.logging.Log;
import de.bixilon.minosoft.util.mojang.api.MojangAccount;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AccountListCell extends ListCell<MojangAccount> implements Initializable {
    public static final ListView<MojangAccount> listView = new ListView<>();

    public MenuButton optionsMenu;
    public Label playerName;
    public MenuItem optionsSelect;
    public Label email;
    public MenuItem optionsDelete;
    public AnchorPane root;

    private MojangAccount account;

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
        setGraphic(root);

        // change locale
        optionsSelect.setText(LocaleManager.translate(Strings.ACCOUNTS_ACTION_SELECT));
        optionsDelete.setText(LocaleManager.translate(Strings.ACCOUNTS_ACTION_DELETE));

    }

    public AnchorPane getRoot() {
        return root;
    }

    @Override
    protected void updateItem(MojangAccount account, boolean empty) {
        super.updateItem(account, empty);

        root.setVisible(!empty);
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
        playerName.setText(account.getPlayerName());
        email.setText(account.getMojangUserName());
        if (Minosoft.getSelectedAccount() == account) {
            setStyle("-fx-background-color: darkseagreen;");
            optionsSelect.setDisable(true);
        }
    }

    private void resetCell() {
        // clear all cells
        setStyle(null);
        optionsSelect.setDisable(false);
    }

    public void delete() {
        account.delete();
        if (Minosoft.getSelectedAccount() == account) {
            if (Minosoft.getConfig().getAccountList().isEmpty()) {
                Minosoft.selectAccount(null);
            } else {
                Minosoft.selectAccount(Minosoft.getConfig().getAccountList().values().iterator().next());
            }
            listView.refresh();
        }
        Log.info(String.format("Deleted account (email=\"%s\", playerName=\"%s\")", account.getMojangUserName(), account.getPlayerName()));
        listView.getItems().remove(account);
    }

    public void clicked(MouseEvent e) {
        switch (e.getButton()) {
            case PRIMARY -> {
                if (e.getClickCount() == 2) {
                    select();
                }
            }
            case SECONDARY -> optionsMenu.fire();
        }
    }

    public void select() {
        Minosoft.selectAccount(account);
        listView.refresh();
        ServerListCell.listView.refresh();
    }
}
