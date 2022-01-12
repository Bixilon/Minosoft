/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.http.QueryUtil.fromQuery
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.gui.eros.controller.JavaFXWindowController
import de.bixilon.minosoft.gui.eros.dialog.ErosErrorReport.Companion.report
import de.bixilon.minosoft.gui.eros.main.account.AccountController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.account.microsoft.MicrosoftOAuthUtils
import javafx.concurrent.Worker
import javafx.fxml.FXML
import javafx.scene.web.WebView
import javafx.stage.Modality
import java.net.URL


class MicrosoftAddController(
    private val accountController: AccountController,
) : JavaFXWindowController() {
    @FXML private lateinit var webView: WebView


    fun show() {
        JavaFXUtil.runLater {
            JavaFXUtil.openModal(TITLE, LAYOUT, this, modality = Modality.APPLICATION_MODAL)
            stage.show()
        }
    }

    override fun init() {
        super.init()
        val profile = ErosProfileManager.selected.general.accountProfile

        JavaFXUtil.resetWebView()
        webView.engine.isJavaScriptEnabled = true
        webView.isContextMenuEnabled = false
        webView.engine.loadContent("Loading...")
        webView.engine.loadWorker.stateProperty().addListener { _, _, new ->
            if (new == Worker.State.SUCCEEDED) {
                val location = webView.engine.location
                if (!location.startsWith("ms-xal-" + ProtocolDefinition.MICROSOFT_ACCOUNT_APPLICATION_ID)) {
                    return@addListener
                }
                JavaFXUtil.resetWebView()

                DefaultThreadPool += {
                    try {
                        // ms-xal-00000000402b5328://auth/?code=M.R3_BL2.9c86df10-b29b-480d-9094-d8accb31e4a5
                        val account = MicrosoftOAuthUtils.loginToMicrosoftAccount(URL(location).query.fromQuery()["code"]!!)
                        profile.entries[account.id] = account
                        profile.selected = account
                        JavaFXUtil.runLater { accountController.refreshList() }
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                        exception.report()
                    }

                    JavaFXUtil.runLater { stage.scene.window.hide() }
                }
            }
        }
        requestOauthFlowToken()
    }

    private fun requestOauthFlowToken() {
        webView.engine.load(ProtocolDefinition.MICROSOFT_ACCOUNT_OAUTH_FLOW_URL)
    }

    companion object {
        private val LAYOUT = "minosoft:eros/main/account/add/microsoft.fxml".toResourceLocation()

        private val TITLE = "minosoft:main.account.add.microsoft.title".toResourceLocation()
    }
}
