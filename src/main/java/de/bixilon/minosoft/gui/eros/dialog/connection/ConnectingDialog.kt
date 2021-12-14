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

import de.bixilon.minosoft.gui.eros.controller.DialogController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.modding.event.events.connection.play.PlayConnectionStateChangeEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.util.KUtil.toResourceLocation
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

    fun show() {
        JavaFXUtil.runLater {
            JavaFXUtil.openModal("TODO", LAYOUT, this)
        }
    }


    override fun init() {
        update(PlayConnectionStates.WAITING)

        connection.registerEvent(CallbackEventInvoker.of<PlayConnectionStateChangeEvent> { update(it.state) }) // ToDo: This creates a memory leak...
    }

    private fun update(state: PlayConnectionStates) {
        if (state == PlayConnectionStates.HANDSHAKING) {
            show()
        }
        val step = state.step
        progressFX.progress = step.toDouble() / (PROGRESS_STEPS - 1).toDouble()
    }

    @FXML
    fun cancel() {
        connection.disconnect()
        stage.hide()
    }

    companion object {
        private val LAYOUT = "minosoft:eros/dialog/connection/connecting.fxml".toResourceLocation()

        private const val PROGRESS_STEPS = 6
        private val PlayConnectionStates.step: Int
            get() = when (this) {
                PlayConnectionStates.ESTABLISHING -> 0
                PlayConnectionStates.HANDSHAKING -> 1
                PlayConnectionStates.LOGGING_IN -> 2
                PlayConnectionStates.JOINING -> 3
                PlayConnectionStates.SPAWNING -> 4
                else -> 5
            }
    }
}
