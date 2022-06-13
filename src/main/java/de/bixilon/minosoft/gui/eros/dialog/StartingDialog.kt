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

package de.bixilon.minosoft.gui.eros.dialog

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.shutdown.ShutdownManager
import de.bixilon.minosoft.ShutdownReasons
import de.bixilon.minosoft.data.text.TranslatableComponents
import de.bixilon.minosoft.gui.eros.controller.DialogController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.ctext
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ProgressBar
import javafx.scene.text.TextFlow

class StartingDialog(
    val latch: CountUpAndDownLatch,
) : DialogController() {
    @FXML private lateinit var headerFX: TextFlow
    @FXML private lateinit var countTextFX: TextFlow
    @FXML private lateinit var progressFX: ProgressBar
    @FXML private lateinit var exitButtonFX: Button

    public override fun show() {
        JavaFXUtil.openModalAsync(TITLE, LAYOUT, this) {
            if (latch.count == 0 || closing) {
                return@openModalAsync
            }
            latch += { JavaFXUtil.runLater { update() } }
            update()
            super.show()
        }
    }


    override fun init() {
        headerFX.text = HEADER
        exitButtonFX.ctext = TranslatableComponents.GENERAL_EXIT
    }

    private fun update() {
        val count = latch.count
        val total = latch.total
        if (count <= 0 && total > 0) {
            stage.close()
            return
        }
        countTextFX.text = "${total - count}/${total}"
        val progress = if (total <= 0) {
            0.0
        } else {
            (total - count.toDouble()) / total.toDouble()
        }
        progressFX.progress = progress
    }

    @FXML
    fun exit() {
        ShutdownManager.shutdown(reason = ShutdownReasons.REQUESTED_BY_USER)
    }

    companion object {
        private val LAYOUT = "minosoft:eros/dialog/starting.fxml".toResourceLocation()

        private val TITLE = "minosoft:dialog.starting.title".toResourceLocation()
        private val HEADER = "minosoft:dialog.starting.header".toResourceLocation()
    }
}
