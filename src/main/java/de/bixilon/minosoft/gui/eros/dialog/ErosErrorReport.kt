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

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.gui.eros.controller.DialogController
import de.bixilon.minosoft.gui.eros.crash.ErosCrashReport.Companion.crash
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.ctext
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import de.bixilon.minosoft.util.KUtil.realName
import de.bixilon.minosoft.util.KUtil.toStackTrace
import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.TextArea
import javafx.scene.text.TextFlow


class ErosErrorReport : DialogController() {
    @FXML private lateinit var headerFX: TextFlow
    @FXML private lateinit var descriptionFX: TextFlow
    @FXML private lateinit var detailsFX: TextArea
    @FXML private lateinit var ignoreFX: Button
    @FXML private lateinit var fatalCrashFX: Button

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

    override fun init() {
        super.init()
        headerFX.text = HEADER
        descriptionFX.text = DESCRIPTION

        ignoreFX.ctext = IGNORE
        fatalCrashFX.ctext = FATAL_CRASH
    }


    companion object {
        private val LAYOUT = "minosoft:eros/dialog/error.fxml".asResourceLocation()
        private val TITLE = { exception: Throwable? -> Minosoft.LANGUAGE_MANAGER.translate("minosoft:error.title".asResourceLocation(), null, exception?.let { it::class.java.realName }) }
        private val HEADER = "minosoft:error.header".asResourceLocation()
        private val DESCRIPTION = "minosoft:error.description".asResourceLocation()
        private val IGNORE = "minosoft:error.ignore".asResourceLocation()
        private val FATAL_CRASH = "minosoft:error.fatal_crash".asResourceLocation()

        fun Throwable?.report() {
            if (RunConfiguration.DISABLE_EROS) {
                return
            }

            Platform.runLater {
                val controller = JavaFXUtil.openModal<ErosErrorReport>(TITLE(this), LAYOUT)
                controller.exception = this
                controller.stage.show()
            }
        }
    }
}
