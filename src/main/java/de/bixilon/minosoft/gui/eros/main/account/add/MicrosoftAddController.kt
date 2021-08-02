/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.eros.main.account.add

import de.bixilon.minosoft.gui.eros.controller.JavaFXWindowController
import de.bixilon.minosoft.gui.eros.main.account.AccountController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import javafx.application.Platform
import javafx.concurrent.Worker
import javafx.fxml.FXML
import javafx.scene.web.WebView
import javafx.stage.Modality
import java.net.CookieHandler
import java.net.CookieManager


class MicrosoftAddController(
    private val accountController: AccountController,
) : JavaFXWindowController() {
    @FXML private lateinit var webView: WebView


    fun show() {
        Platform.runLater {
            JavaFXUtil.openModal(TITLE, LAYOUT, this, modality = Modality.APPLICATION_MODAL)
            stage.show()
        }
    }

    override fun init() {
        super.init()

        CookieHandler.setDefault(CookieManager())

        webView.engine.isJavaScriptEnabled = true
        webView.isContextMenuEnabled = false
        webView.engine.loadContent("Loading...")
        webView.engine.loadWorker.stateProperty().addListener { _, _, new ->
            if (new == Worker.State.SUCCEEDED) {
                if (webView.engine.location.startsWith("ms-xal-" + ProtocolDefinition.MICROSOFT_ACCOUNT_APPLICATION_ID)) {
                    // login is being handled by MicrosoftOAuthUtils. We can go now...
                    stage.scene.window.hide()
                }
            }
        }
        requestOauthFlowToken()
    }

    private fun requestOauthFlowToken() {
        webView.engine.load(ProtocolDefinition.MICROSOFT_ACCOUNT_OAUTH_FLOW_URL)
    }

    companion object {
        private val LAYOUT = "minosoft:eros/main/account/add/microsoft.fxml".asResourceLocation()

        private val TITLE = "minosoft:main.account.add.microsoft.title".asResourceLocation()
    }
}
