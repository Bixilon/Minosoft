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

package de.bixilon.minosoft.gui.eros.dialog

import de.bixilon.minosoft.gui.eros.controller.JavaFXWindowController
import de.bixilon.minosoft.gui.eros.crash.ErosCrashReport.Companion.crash
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import de.bixilon.minosoft.util.KUtil.toStackTrace
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.TextArea


class ErosErrorReport : JavaFXWindowController() {
    @FXML private lateinit var detailsFX: TextArea

    var exception: Throwable? = null
        set(value) {
            field = value
            detailsFX.text = exception?.toStackTrace()
        }


    @FXML
    fun ignore() {
        stage.close()
    }

    @FXML
    fun fatalCrash() {
        stage.close()
        exception?.crash()
    }


    companion object {
        private val LAYOUT = "minosoft:eros/dialog/error.fxml".asResourceLocation()

        fun Throwable?.report() {
            if (RunConfiguration.DISABLE_EROS) {
                return
            }

            Platform.runLater {
                val controller = JavaFXUtil.openModal<ErosErrorReport>("", LAYOUT)
                controller.exception = this
                controller.stage.show()
            }
        }
    }
}
