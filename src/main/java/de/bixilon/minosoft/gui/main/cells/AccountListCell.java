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

package de.bixilon.minosoft.gui.main.cells;

import com.jfoenix.controls.JFXButton;
import de.bixilon.minosoft.Minosoft;
import de.bixilon.minosoft.data.accounts.Account;
import de.bixilon.minosoft.data.locale.LocaleManager;
import de.bixilon.minosoft.data.locale.Strings;
import de.bixilon.minosoft.gui.main.GUITools;
import de.bixilon.minosoft.util.logging.Log;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class AccountListCell extends ListCell<Account> implements Initializable {
    public static final ListView<Account> ACCOUNT_LIST_VIEW = new ListView<>();
    public HBox hBox;
    public ImageView head;
    public Label username;
    public Label type;

    public JFXButton selectIcon;
    public JFXButton infoIcon;
    public JFXButton logoutIcon;


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
        setGraphic(this.hBox);

        // change locale
        this.selectIcon.setText(LocaleManager.translate(Strings.ACCOUNTS_ACTION_SELECT));
        this.infoIcon.setText(LocaleManager.translate(Strings.ACCOUNTS_ACTION_INFO));
        this.logoutIcon.setText(LocaleManager.translate(Strings.ACCOUNTS_ACTION_LOGOUT));

    }

    @Override
    protected void updateItem(Account account, boolean empty) {
        super.updateItem(account, empty);

        this.hBox.setVisible(account != null || !empty);
        if (empty) {
            resetCell();
            return;
        }

        if (account == null) {
            resetCell();
            return;
        }

        resetCell();
        if (Minosoft.getConfig().getSelectedAccount() == account) {
            this.hBox.getStyleClass().add("list-cell-selected");
            this.selectIcon.setDisable(true);
        }

        // ToDo: Set head

        this.account = account;
        this.username.setText(account.getUsername());
        this.type.setText(account.getClass().getSimpleName());
    }

    private void resetCell() {
        // clear all cells
        this.hBox.getStyleClass().removeAll("list-cell-selected");
        this.selectIcon.setDisable(false);
        this.head.setImage(GUITools.MINOSOFT_LOGO);
    }


    public void select() {
        Minosoft.selectAccount(this.account);

        if (Minosoft.getConfig().getSelectedAccount() == this.account) {
            // ToDo: Why isn't his working correct?
            this.hBox.getStyleClass().add("list-cell-selected");
            this.selectIcon.setDisable(true);
        }
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
            ACCOUNT_LIST_VIEW.refresh();
        }
        Log.info(String.format("Deleted account (type=%s, id=%s, username=%s)", this.account.getClass().getSimpleName(), this.account.getId(), this.account.getUsername()));
        ACCOUNT_LIST_VIEW.getItems().remove(this.account);
    }

    public void info() {
        // ToDo
    }

    public void clicked(MouseEvent e) {
        if (e.getButton() != MouseButton.PRIMARY) {
            return;
        }
        if (e.getClickCount() != 2) {
            return;
        }

        select();
    }

}
