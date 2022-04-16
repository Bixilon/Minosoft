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

import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.data.accounts.types.microsoft.MicrosoftAccount
import de.bixilon.minosoft.gui.eros.controller.JavaFXWindowController
import de.bixilon.minosoft.gui.eros.main.account.AccountController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.fxml.FXML
import javafx.scene.control.TextField
import javafx.scene.text.TextFlow
import javafx.stage.Modality


class MicrosoftAddController(
    private val accountController: AccountController,
    private val account: MicrosoftAccount? = null,
) : JavaFXWindowController() {
    @FXML private lateinit var textFX: TextFlow
    @FXML private lateinit var codeFX: TextField


    fun show() {
        JavaFXUtil.runLater {
            JavaFXUtil.openModal(TITLE, LAYOUT, this, modality = Modality.APPLICATION_MODAL)
            stage.show()
        }
    }

    override fun init() {
        super.init()
        val profile = ErosProfileManager.selected.general.accountProfile

    }

    companion object {
        private val LAYOUT = "minosoft:eros/main/account/add/microsoft.fxml".toResourceLocation()
        private val TITLE = "minosoft:main.account.add.microsoft.title".toResourceLocation()
    }
}
