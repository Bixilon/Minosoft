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

package de.bixilon.minosoft.gui.eros.main.account

import de.bixilon.kutil.latch.CallbackLatch
import de.bixilon.minosoft.gui.eros.controller.DialogController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ProgressBar
import javafx.scene.text.TextFlow

class CheckingDialog(
    val latch: CallbackLatch,
) : DialogController() {
    @FXML private lateinit var headerFX: TextFlow
    @FXML private lateinit var progressFX: ProgressBar
    @FXML private lateinit var cancelButtonFX: Button


    public override fun show() {
        JavaFXUtil.openModalAsync(TITLE, LAYOUT, this) {
            if (closing) {
                return@openModalAsync
            }
            latch += { update() }
            update()
            super.show()
        }
    }

    override fun init() {
        headerFX.text = HEADER
    }

    private fun update() {
        if (latch.count == 0) {
            return close()
        }
        val progress = 1.0 - (latch.count.toDouble() / latch.total)
        progressFX.progress = progress
    }

    @FXML
    fun cancel() {
        TODO("Not yet implemented!")
    }

    companion object {
        private val LAYOUT = "minosoft:eros/main/account/checking.fxml".toResourceLocation()

        private val TITLE = "minosoft:main.account.checking_dialog.title".toResourceLocation()
        private val HEADER = "minosoft:main.account.checking_dialog.header".toResourceLocation()
    }
}
