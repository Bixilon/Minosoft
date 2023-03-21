/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.data.accounts.types.microsoft.MicrosoftAccount
import de.bixilon.minosoft.gui.eros.controller.JavaFXWindowController
import de.bixilon.minosoft.gui.eros.dialog.ErosErrorReport.Companion.report
import de.bixilon.minosoft.gui.eros.dialog.PleaseWaitDialog
import de.bixilon.minosoft.gui.eros.main.account.AccountController
import de.bixilon.minosoft.gui.eros.main.account.CheckingDialog
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.ctext
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.account.microsoft.AuthenticationResponse
import de.bixilon.minosoft.util.account.microsoft.MicrosoftOAuthUtils
import de.bixilon.minosoft.util.account.microsoft.code.MicrosoftDeviceCode
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.text.TextFlow
import javafx.stage.Modality
import java.net.URL


class MicrosoftAddController(
    private val accountController: AccountController,
    private val account: MicrosoftAccount? = null,
) : JavaFXWindowController() {
    private val profile = ErosProfileManager.selected.general.accountProfile
    @FXML private lateinit var headerFX: TextFlow
    @FXML private lateinit var codeFX: TextField
    @FXML private lateinit var cancelFX: Button

    private var deviceCodeDialog: PleaseWaitDialog? = null

    fun request() {
        deviceCodeDialog = PleaseWaitDialog(header = "minosoft:main.account.add.microsoft.please_wait.device_code".toResourceLocation())
        deviceCodeDialog?.show()
        MicrosoftOAuthUtils.obtainDeviceCodeAsync(this::codeCallback, this::errorCallback, this::successCallback)
    }

    private fun errorCallback(exception: Throwable) {
        deviceCodeDialog?.close()
        JavaFXUtil.runLater { stage.close() }
        exception.report()
    }

    private fun codeCallback(code: MicrosoftDeviceCode) {
        JavaFXUtil.runLater {
            deviceCodeDialog?.close()
            JavaFXUtil.openModal(TITLE, LAYOUT, this, modality = Modality.APPLICATION_MODAL)
            headerFX.text = HEADER(code.verificationURI)
            codeFX.text = code.userCode
            stage.show()
        }
    }

    private fun successCallback(response: AuthenticationResponse) {
        JavaFXUtil.runLater { close() }
        val latch = CountUpAndDownLatch(1)
        val checkingDialog = CheckingDialog(latch)
        checkingDialog.show()

        try {
            val account = MicrosoftOAuthUtils.loginToMicrosoftAccount(response, latch)
            profile.entries[account.id] = account
            if (this.account == null) {
                profile.selected = account
            }
            latch.dec()
            JavaFXUtil.runLater {
                checkingDialog.close()
                accountController.refreshList()
            }
        } catch (exception: Throwable) {
            latch.count = 0
            checkingDialog.close()
            exception.printStackTrace()
            exception.report()
        }
    }

    override fun init() {
        super.init()
        cancelFX.ctext = CANCEL
    }

    @FXML
    fun cancel() {
        TODO()
    }

    companion object {
        private val LAYOUT = "minosoft:eros/main/account/add/microsoft.fxml".toResourceLocation()
        private val TITLE = "minosoft:main.account.add.microsoft.title".toResourceLocation()
        private val HEADER = { link: URL -> Minosoft.LANGUAGE_MANAGER.forceTranslate("minosoft:main.account.add.microsoft.header".toResourceLocation(), link) }
        private val CANCEL = "minosoft:main.account.add.microsoft.cancel".toResourceLocation()
    }
}
