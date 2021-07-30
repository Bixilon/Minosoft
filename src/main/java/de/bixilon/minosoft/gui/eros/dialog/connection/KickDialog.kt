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

package de.bixilon.minosoft.gui.eros.dialog.connection

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.eros.controller.JavaFXWindowController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.text.TextFlow

class KickDialog(
    val title: Any,
    val header: Any,
    val description: Any? = null,
    val reason: ChatComponent,
) : JavaFXWindowController() {
    @FXML private lateinit var headerFX: TextFlow
    @FXML private lateinit var descriptionFX: TextFlow
    @FXML private lateinit var reasonFX: TextFlow
    @FXML private lateinit var reconnectButtonFX: Button
    @FXML private lateinit var closeButtonFX: Button

    fun show() {
        Platform.runLater {
            JavaFXUtil.openModal(title, LAYOUT, this)
            stage.show()
        }
    }


    override fun init() {
        headerFX.text = Minosoft.LANGUAGE_MANAGER.translate(header)
        descriptionFX.text = description?.let { Minosoft.LANGUAGE_MANAGER.translate(it) } ?: ChatComponent.EMPTY
        reasonFX.text = reason

        reconnectButtonFX.isDisable = true // ToDo
        closeButtonFX.setOnAction {
            stage.hide()
        }
    }

    companion object {
        private val LAYOUT = "minosoft:eros/dialog/connection/kick.fxml".asResourceLocation()
    }
}
