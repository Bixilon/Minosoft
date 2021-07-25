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

import de.bixilon.minosoft.config.server.Server
import de.bixilon.minosoft.data.text.TranslatableComponents
import de.bixilon.minosoft.gui.eros.controller.JavaFXWindowController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.ctext
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.placeholder
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.DNSUtil
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import de.bixilon.minosoft.util.KUtil.decide
import de.bixilon.minosoft.util.task.pool.DefaultThreadPool
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextField
import javafx.scene.text.TextFlow

/**
 * Used to add or edit a server
 */
class UpdateServerDialog(
    private val server: Server? = null,
    val onCancel: () -> Unit = {},
    val onUpdate: (name: String, address: String) -> Unit,
) : JavaFXWindowController() {
    @FXML
    lateinit var descriptionFX: TextFlow

    @FXML
    lateinit var serverNameLabelFX: TextFlow

    @FXML
    lateinit var serverNameFX: TextField

    @FXML
    lateinit var serverAddressLabelFX: TextFlow

    @FXML
    lateinit var serverAddressFX: TextField

    @FXML
    lateinit var updateServerButtonFX: Button

    @FXML
    lateinit var cancelButtonFX: Button


    fun show() {
        Platform.runLater {
            JavaFXUtil.openModal((server == null).decide(ADD_TITLE, EDIT_TITLE), LAYOUT, this)
            stage.show()
        }
    }

    override fun init() {
        serverNameLabelFX.text = SERVER_NAME_LABEL
        serverNameFX.placeholder = SERVER_NAME_PLACEHOLDER
        serverAddressLabelFX.text = SERVER_ADDRESS_LABEL
        serverAddressFX.placeholder = SERVER_ADDRESS_PLACEHOLDER

        cancelButtonFX.ctext = TranslatableComponents.GENERAL_CANCEL


        if (server == null) {
            // add
            descriptionFX.text = ADD_DESCRIPTION
            updateServerButtonFX.ctext = ADD_UPDATE_BUTTON
        } else {
            descriptionFX.text = EDIT_DESCRIPTION
            updateServerButtonFX.ctext = EDIT_UPDATE_BUTTON

            serverNameFX.text = server.name.legacyText.removeSuffix("§r")
            serverAddressFX.text = server.address

            updateServerButtonFX.isDisable = serverAddressFX.text.isBlank()
        }

        serverAddressFX.textProperty().addListener { _, _, new ->
            serverAddressFX.text = DNSUtil.fixAddress(new)

            updateServerButtonFX.isDisable = serverAddressFX.text.isBlank()
        }
    }

    @FXML
    fun update() {
        DefaultThreadPool += { onUpdate(serverNameFX.text.isBlank().decide({ serverAddressFX.text.toString() }, { serverNameFX.text.trim() }), serverAddressFX.text) }
        stage.close()
    }

    @FXML
    fun cancel() {
        DefaultThreadPool += onCancel
        stage.close()
    }


    companion object {
        private val LAYOUT = "minosoft:eros/dialog/update_server.fxml".asResourceLocation()

        private val SERVER_NAME_LABEL = "minosoft:update_server.name.label".asResourceLocation()
        private val SERVER_NAME_PLACEHOLDER = "minosoft:update_server.name.placeholder".asResourceLocation()
        private val SERVER_ADDRESS_LABEL = "minosoft:update_server.address.label".asResourceLocation()
        private val SERVER_ADDRESS_PLACEHOLDER = "minosoft:update_server.address.placeholder".asResourceLocation()

        private val ADD_TITLE = "minosoft:update_server.add.title".asResourceLocation()
        private val ADD_DESCRIPTION = "minosoft:update_server.add.description".asResourceLocation()
        private val ADD_UPDATE_BUTTON = "minosoft:update_server.add.update_button".asResourceLocation()


        private val EDIT_TITLE = "minosoft:update_server.edit.title".asResourceLocation()
        private val EDIT_DESCRIPTION = "minosoft:update_server.edit.description".asResourceLocation()
        private val EDIT_UPDATE_BUTTON = "minosoft:update_server.edit.update_button".asResourceLocation()
    }
}
