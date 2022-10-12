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

import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.gui.eros.dialog.progress.ProgressDialog
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.fxml.FXML
import javafx.scene.text.TextFlow

class VerifyAssetsDialog(
    latch: CountUpAndDownLatch,
) : ProgressDialog(title = TITLE, header = HEADER, latch = latch, layout = LAYOUT) {
    @FXML private lateinit var mibTextFX: TextFlow

    override fun updateUI(count: Int, total: Int, progress: Double) {
        super.updateUI(count, total, progress)
        mibTextFX.text = "<TBA> MiB"
    }

    companion object {
        private val LAYOUT = "minosoft:eros/dialog/connection/verify_assets.fxml".toResourceLocation()

        private val TITLE = "minosoft:connection.dialog.verify_assets.title".toResourceLocation()
        private val HEADER = "minosoft:connection.dialog.verify_assets.header".toResourceLocation()
    }
}
