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

package de.bixilon.minosoft.gui.eros.dialog.connection

import de.bixilon.minosoft.gui.eros.controller.DialogController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.delegate.JavaFXDelegate.observeFX
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ProgressBar
import javafx.scene.text.TextFlow

class ConnectingDialog(
    val connection: PlayConnection,
) : DialogController() {
    @FXML private lateinit var headerFX: TextFlow
    @FXML private lateinit var statusTextFX: TextFlow
    @FXML private lateinit var progressFX: ProgressBar
    @FXML private lateinit var cancelButtonFX: Button

    public override fun show() {
        JavaFXUtil.openModalAsync(TITLE, LAYOUT, this) { update(connection.state) }
    }


    override fun init() {
        headerFX.text = HEADER
        cancelButtonFX.isDisable = true
        stage.onCloseRequest = EventHandler {
            it.consume()
        }
        connection::state.observeFX(this) { update(it) }
    }

    private fun update(state: PlayConnectionStates) {
        val step = state.step
        if (!stage.isShowing && step >= 0) {
            super.show()
        }
        progressFX.progress = step.toDouble() / (PROGRESS_STEPS - 1).toDouble()
        if (progressFX.progress == 1.0) {
            return close()
        }
        statusTextFX.text = state
    }

    @FXML
    fun cancel() {
        connection.network.disconnect()
        close()
    }

    companion object {
        private val LAYOUT = "minosoft:eros/dialog/connection/connecting.fxml".toResourceLocation()

        private val TITLE = "minosoft:connection.dialog.connecting.title".toResourceLocation()
        private val HEADER = "minosoft:connection.dialog.connecting.header".toResourceLocation()

        private const val PROGRESS_STEPS = 7
        private val PlayConnectionStates.step: Int
            get() = when (this) {
                PlayConnectionStates.LOADING -> 0
                PlayConnectionStates.ESTABLISHING -> 1
                PlayConnectionStates.HANDSHAKING -> 2
                PlayConnectionStates.LOGGING_IN -> 3
                PlayConnectionStates.JOINING -> 4
                PlayConnectionStates.SPAWNING -> 5
                else -> 6
            }
    }
}
