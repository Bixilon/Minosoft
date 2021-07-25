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

package de.bixilon.minosoft.gui.eros.dialogs

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.eros.controller.JavaFXWindowController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import de.bixilon.minosoft.util.task.pool.DefaultThreadPool
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.text.TextFlow

class SimpleErosConfirmationDialog(
    val title: Any = DEFAULT_TITLE_TEXT,
    val header: Any = DEFAULT_TITLE_TEXT,
    val description: Any? = null,
    val cancelButtonText: Any = DEFAULT_CANCEL_TEXT,
    val confirmButtonText: Any = DEFAULT_CONFIRM_TEXT,
    val onCancel: () -> Unit = {},
    val onConfirm: () -> Unit,
) : JavaFXWindowController() {
    @FXML
    lateinit var headerFX: TextFlow
    @FXML
    lateinit var descriptionFX: TextFlow
    @FXML
    lateinit var cancelButtonFX: Button
    @FXML
    lateinit var confirmButtonFX: Button

    fun show() {
        Platform.runLater {
            JavaFXUtil.openModal(title, LAYOUT, this)
            stage.show()
        }
    }

    override fun init() {
        headerFX.text = Minosoft.LANGUAGE_MANAGER.translate(header)
        descriptionFX.text = description?.let { Minosoft.LANGUAGE_MANAGER.translate(it) } ?: ChatComponent.EMPTY
        cancelButtonFX.text = Minosoft.LANGUAGE_MANAGER.translate(cancelButtonText).message
        confirmButtonFX.text = Minosoft.LANGUAGE_MANAGER.translate(confirmButtonText).message
    }

    @FXML
    fun confirm() {
        DefaultThreadPool += onConfirm
        stage.hide()
    }

    @FXML
    fun cancel() {
        DefaultThreadPool += onCancel
        stage.hide()
    }


    companion object {
        private val LAYOUT = "minosoft:eros/dialog/simple_confirmation.fxml".asResourceLocation()
        private val DEFAULT_TITLE_TEXT = "minosoft:general.dialog.are_you_sure".asResourceLocation()
        private val DEFAULT_CANCEL_TEXT = "minosoft:general.cancel".asResourceLocation()
        private val DEFAULT_CONFIRM_TEXT = "minosoft:general.confirm".asResourceLocation()
    }
}
