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

import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.data.accounts.types.OfflineAccount
import de.bixilon.minosoft.gui.eros.controller.JavaFXWindowController
import de.bixilon.minosoft.gui.eros.main.account.AccountController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.ctext
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.placeholder
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.text.TextFlow
import javafx.stage.Modality

class OfflineAddController(
    private val accountController: AccountController,
) : JavaFXWindowController() {
    private val accountProfile = ErosProfileManager.selected.general.accountProfile
    @FXML private lateinit var headerFX: TextFlow
    @FXML private lateinit var descriptionFX: TextFlow

    @FXML private lateinit var usernameLabelFX: TextFlow
    @FXML private lateinit var usernameFX: TextField

    @FXML private lateinit var addButtonFX: Button
    @FXML private lateinit var cancelButtonFX: Button


    fun show() {
        JavaFXUtil.openModalAsync(TITLE, LAYOUT, this, modality = Modality.APPLICATION_MODAL) {
            stage.show()
        }
    }

    override fun init() {
        super.init()


        headerFX.text = HEADER
        descriptionFX.text = DESCRIPTION
        usernameLabelFX.text = USERNAME_LABEL
        usernameFX.placeholder = USERNAME_PLACEHOLDER
        addButtonFX.ctext = ADD_BUTTON
        cancelButtonFX.ctext = CANCEL_BUTTON

        usernameFX.textProperty().addListener { _, _, new ->
            addButtonFX.isDisable = !ProtocolDefinition.MINECRAFT_NAME_VALIDATOR.matcher(new).matches() || accountProfile.entries.containsKey(new)
        }
    }

    override fun postInit() {
        super.postInit()

        stage.scene.root.addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.ESCAPE) {
                cancel()
            }
        }
    }

    @FXML
    fun add() {
        if (addButtonFX.isDisable) {
            return
        }
        val account = OfflineAccount(usernameFX.text)
        accountProfile.entries[account.id] = account
        accountProfile.selected = account

        accountController.refreshList()
        stage.hide()
    }

    @FXML
    fun cancel() {
        stage.hide()
    }

    companion object {
        private val LAYOUT = "minosoft:eros/main/account/add/offline.fxml".toResourceLocation()

        private val TITLE = "minosoft:main.account.add.offline.title".toResourceLocation()
        private val HEADER = "minosoft:main.account.add.offline.header".toResourceLocation()
        private val DESCRIPTION = "minosoft:main.account.add.offline.description".toResourceLocation()
        private val USERNAME_LABEL = "minosoft:main.account.add.offline.username.label".toResourceLocation()
        private val USERNAME_PLACEHOLDER = "minosoft:main.account.add.offline.username.placeholder".toResourceLocation()
        private val ADD_BUTTON = "minosoft:main.account.add.offline.add_button".toResourceLocation()
        private val CANCEL_BUTTON = "minosoft:main.account.add.offline.cancel_button".toResourceLocation()
    }
}
