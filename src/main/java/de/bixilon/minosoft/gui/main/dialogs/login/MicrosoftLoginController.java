/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.main.dialogs.login;

import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import javafx.scene.web.WebView;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.util.ResourceBundle;

public class MicrosoftLoginController implements Initializable {
    public HBox hBox;
    public WebView webView;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        CookieHandler.setDefault(new CookieManager());

        this.webView.getEngine().setJavaScriptEnabled(true);
        this.webView.setContextMenuEnabled(false);
        this.webView.getEngine().loadContent("Loading...");
        this.webView.getEngine().getLoadWorker().stateProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == Worker.State.SUCCEEDED) {
                if (this.webView.getEngine().getLocation().startsWith("ms-xal-" + ProtocolDefinition.MICROSOFT_ACCOUNT_APPLICATION_ID)) {
                    // login is being handled by MicrosoftOAuthUtils. We can go now...
                    this.hBox.getScene().getWindow().hide();
                }
            }
        });
        requestOauthFlowToken();
    }

    private void requestOauthFlowToken() {
        this.webView.getEngine().load(ProtocolDefinition.MICROSOFT_ACCOUNT_OAUTH_FLOW_URL);
    }

    public void login(ActionEvent event) {
        event.consume();
    }
}
