/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.eros.dialog.session

import de.bixilon.minosoft.gui.eros.controller.DialogController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.network.session.play.PlaySessionStates
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.delegate.JavaFXDelegate.observeFX
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ProgressBar
import javafx.scene.text.TextFlow

class ConnectingDialog(
    val session: PlaySession,
) : DialogController() {
    @FXML private lateinit var headerFX: TextFlow
    @FXML private lateinit var statusTextFX: TextFlow
    @FXML private lateinit var progressFX: ProgressBar
    @FXML private lateinit var cancelButtonFX: Button

    public override fun show() {
        JavaFXUtil.openModalAsync(TITLE, LAYOUT, this) { update(session.state) }
    }

    override fun init() {
        headerFX.text = HEADER
        cancelButtonFX.isDisable = true
        session::state.observeFX(this) { update(it) }
    }

    override fun postInit() {
        super.postInit()

        stage.onCloseRequest = EventHandler {
            it.consume()
        }
    }

    private fun update(state: PlaySessionStates) {
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
        session.network.disconnect()
        close()
    }

    companion object {
        private val LAYOUT = "minosoft:eros/dialog/session/connecting.fxml".toResourceLocation()

        private val TITLE = "minosoft:session.dialog.connecting.title".toResourceLocation()
        private val HEADER = "minosoft:session.dialog.connecting.header".toResourceLocation()

        private const val PROGRESS_STEPS = 7
        private val PlaySessionStates.step: Int
            get() = when (this) {
                PlaySessionStates.LOADING -> 0
                PlaySessionStates.ESTABLISHING -> 1
                PlaySessionStates.HANDSHAKING -> 2
                PlaySessionStates.LOGGING_IN -> 3
                PlaySessionStates.JOINING -> 4
                PlaySessionStates.SPAWNING -> 5
                else -> 6
            }
    }
}
