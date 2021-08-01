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

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.accounts.types.MojangAccount
import de.bixilon.minosoft.gui.eros.controller.JavaFXWindowController
import de.bixilon.minosoft.gui.eros.main.account.AccountController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.ctext
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.placeholder
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import de.bixilon.minosoft.util.KUtil.text
import de.bixilon.minosoft.util.task.pool.DefaultThreadPool
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.PasswordField
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.text.TextFlow
import javafx.stage.Modality

class MojangAddController(
    private val accountController: AccountController,
) : JavaFXWindowController() {
    @FXML private lateinit var headerFX: TextFlow
    @FXML private lateinit var descriptionFX: TextFlow

    @FXML private lateinit var emailLabelFX: TextFlow
    @FXML private lateinit var emailFX: TextField

    @FXML private lateinit var passwordLabelFX: TextFlow
    @FXML private lateinit var passwordFX: PasswordField

    @FXML private lateinit var errorFX: TextFlow

    @FXML private lateinit var loginButtonFX: Button
    @FXML private lateinit var cancelButtonFX: Button


    fun show() {
        Platform.runLater {
            JavaFXUtil.openModal(TITLE, LAYOUT, this, modality = Modality.APPLICATION_MODAL)
            stage.show()
        }
    }

    override fun init() {
        super.init()


        headerFX.text = HEADER
        descriptionFX.text = DESCRIPTION

        emailLabelFX.text = EMAIL_LABEL
        emailFX.placeholder = EMAIL_PLACEHOLDER
        passwordLabelFX.text = PASSWORD_LABEL
        passwordFX.placeholder = PASSWORD_PLACEHOLDER

        loginButtonFX.ctext = ADD_BUTTON
        cancelButtonFX.ctext = CANCEL_BUTTON

        errorFX.isVisible = false
        emailFX.textProperty().addListener { _, _, _ ->
            validate()
        }
        passwordFX.textProperty().addListener { _, _, _ ->
            validate()
        }
        stage.scene.root.addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.ENTER) {
                login()
            }
        }
    }

    private fun validate() {
        if (emailFX.text.isBlank()) {
            loginButtonFX.isDisable = true
            return
        }
        if (passwordFX.text.isEmpty()) {
            loginButtonFX.isDisable = true
            return
        }
        loginButtonFX.isDisable = false
    }

    @FXML
    fun login() {
        if (loginButtonFX.isDisable) {
            return
        }
        loginButtonFX.isDisable = true
        errorFX.isVisible = false
        DefaultThreadPool += {
            try {
                val account = MojangAccount.login(email = emailFX.text, password = passwordFX.text)
                Minosoft.config.config.account.entries[account.id] = account
                Minosoft.config.saveToFile()
                Platform.runLater {
                    accountController.refreshList()
                    stage.hide()
                }
            } catch (exception: Exception) {
                Platform.runLater {
                    exception.printStackTrace()
                    errorFX.text = exception.text
                    errorFX.isVisible = true
                    loginButtonFX.isDisable = false
                }
            }
        }
    }

    @FXML
    fun cancel() {
        stage.hide()
    }

    companion object {
        private val LAYOUT = "minosoft:eros/main/account/add/mojang.fxml".asResourceLocation()

        private val TITLE = "minosoft:main.account.add.mojang.title".asResourceLocation()
        private val HEADER = "minosoft:main.account.add.mojang.header".asResourceLocation()
        private val DESCRIPTION = "minosoft:main.account.add.mojang.description".asResourceLocation()
        private val EMAIL_LABEL = "minosoft:main.account.add.mojang.email.label".asResourceLocation()
        private val EMAIL_PLACEHOLDER = "minosoft:main.account.add.mojang.email.placeholder".asResourceLocation()
        private val PASSWORD_LABEL = "minosoft:main.account.add.mojang.password.label".asResourceLocation()
        private val PASSWORD_PLACEHOLDER = "minosoft:main.account.add.mojang.password.placeholder".asResourceLocation()
        private val ADD_BUTTON = "minosoft:main.account.add.mojang.add_button".asResourceLocation()
        private val CANCEL_BUTTON = "minosoft:main.account.add.mojang.cancel_button".asResourceLocation()
    }
}
