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

import de.bixilon.minosoft.gui.eros.controller.DialogController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ProgressBar
import javafx.scene.text.TextFlow

class PleaseWaitDialog(
    private val title: Any = TITLE,
    private val header: Any = HEADER,
    private val onCancel: (() -> Unit)? = null,
) : DialogController() {
    @FXML private lateinit var headerFX: TextFlow
    @FXML private lateinit var progressFX: ProgressBar
    @FXML private lateinit var cancelButtonFX: Button


    public override fun show() {
        JavaFXUtil.openModalAsync(title, LAYOUT, this) { super.show() }
    }

    override fun init() {
        headerFX.text = header
        if (onCancel != null) {
            cancelButtonFX.isDisable = false
        }
    }


    @FXML
    fun cancel() {
        onCancel?.invoke()
    }

    companion object {
        private val LAYOUT = "minosoft:eros/main/account/checking.fxml".toResourceLocation()

        private val TITLE = "minosoft:main.account.please_wait.title".toResourceLocation()
        private val HEADER = "minosoft:main.account.please_wait.header".toResourceLocation()
    }
}
