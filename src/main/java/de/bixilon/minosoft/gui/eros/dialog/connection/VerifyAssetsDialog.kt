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

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.gui.eros.controller.DialogController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ProgressBar
import javafx.scene.text.TextFlow

class VerifyAssetsDialog(
    val latch: CountUpAndDownLatch,
) : DialogController() {
    @FXML private lateinit var headerFX: TextFlow
    @FXML private lateinit var countTextFX: TextFlow
    @FXML private lateinit var mibTextFX: TextFlow
    @FXML private lateinit var progressFX: ProgressBar
    @FXML private lateinit var cancelButtonFX: Button

    fun show() {
        JavaFXUtil.runLater {
            JavaFXUtil.openModal(TITLE, LAYOUT, this)
            update()
            stage.show()
        }
    }


    override fun init() {
        headerFX.text = HEADER
        cancelButtonFX.isDisable = true
        latch += {
            JavaFXUtil.runLater {
                update()
            }
        }
    }

    private fun update() {
        val count = latch.count
        val total = latch.total
        if (count <= 0 && total > 0) {
            stage.close()
            return
        }
        countTextFX.text = "${total - count}/${total}"
        mibTextFX.text = "No clue how much MiB :)"
        val progress = if (total <= 0) {
            0.0
        } else {
            (total - count.toDouble()) / total.toDouble()
        }
        progressFX.progress = progress
    }

    @FXML
    fun cancel() {
        TODO("Not yet implemented")
    }

    companion object {
        private val LAYOUT = "minosoft:eros/dialog/connection/verify_assets.fxml".toResourceLocation()

        private val TITLE = "minosoft:connection.dialog.verify_assets.title".toResourceLocation()
        private val HEADER = "minosoft:connection.dialog.verify_assets.header".toResourceLocation()
    }
}
