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

import de.bixilon.kutil.exception.ExceptionUtil.toStackTrace
import de.bixilon.kutil.reflection.ReflectionUtil.realName
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.text.TranslatableComponents.GENERAL_IGNORE
import de.bixilon.minosoft.gui.eros.controller.DialogController
import de.bixilon.minosoft.gui.eros.crash.ErosCrashReport.Companion.crash
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.ctext
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.KUtil.toResourceLocation
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

        ignoreFX.ctext = GENERAL_IGNORE
        fatalCrashFX.ctext = FATAL_CRASH
    }


    companion object {
        private val LAYOUT = "minosoft:eros/dialog/error.fxml".toResourceLocation()
        private val TITLE = { exception: Throwable? -> Minosoft.LANGUAGE_MANAGER.translate("minosoft:error.title".toResourceLocation(), null, exception?.let { it::class.java.realName }) }
        private val HEADER = "minosoft:error.header".toResourceLocation()
        private val DESCRIPTION = "minosoft:error.description".toResourceLocation()
        private val FATAL_CRASH = "minosoft:error.fatal_crash".toResourceLocation()

        fun Throwable?.report() {
            if (RunConfiguration.DISABLE_EROS) {
                return
            }

            JavaFXUtil.runLater {
                val controller = JavaFXUtil.openModal<ErosErrorReport>(TITLE(this), LAYOUT)
                controller.exception = this
                controller.stage.show()
            }
        }
    }
}
