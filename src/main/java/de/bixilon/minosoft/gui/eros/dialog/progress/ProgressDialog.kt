/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.eros.dialog.progress

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.latch.CallbackLatch
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.eros.controller.DialogController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ProgressBar
import javafx.scene.text.TextFlow

open class ProgressDialog(
    private val title: ResourceLocation,
    private val header: ResourceLocation,
    val latch: CallbackLatch,
    private val onCancel: (() -> Unit)? = null,
    private val layout: ResourceLocation = LAYOUT,
) : DialogController() {
    @FXML private lateinit var headerFX: TextFlow
    @FXML private lateinit var countTextFX: TextFlow
    @FXML private lateinit var progressFX: ProgressBar
    @FXML private lateinit var cancelButtonFX: Button

    private var step = Int.MAX_VALUE

    public override fun show() {
        JavaFXUtil.runLater {
            JavaFXUtil.openModal(title, layout, this)
            latch += { update() }
            update()
            super.show()
        }
    }


    override fun init() {
        headerFX.text = header
        cancelButtonFX.isDisable = onCancel == null
    }

    override fun postInit() {
        super.postInit()
        if (onCancel == null) {
            stage.onCloseRequest = EventHandler {
                it.consume()
            }
        }
    }

    private fun update() {
        val count = latch.count
        val total = latch.total
        val progress = getProgress(count, total)
        val step = progress.steps()
        if (step == this.step) {
            return
        }
        this.step = step
        JavaFXUtil.runLater { _update(count, total, progress) }
    }

    private fun _update(count: Int, total: Int, progress: Double) {
        if (progress.steps() != this.step) {
            return
        }
        if (count <= 0 && total > 0) {
            stage.close()
            return
        }
        updateUI(count, total, progress)
    }

    protected open fun updateUI(count: Int, total: Int, progress: Double) {
        countTextFX.text = "${total - count}/${total}"
        progressFX.progress = progress
    }

    private fun getProgress(count: Int, total: Int): Double {
        return if (total <= 0) {
            0.0
        } else {
            (total - count.toDouble()) / total.toDouble()
        }
    }

    @FXML
    open fun cancel() {
        val onCancel = this.onCancel ?: throw IllegalStateException("Cancel invoked, but callback is null!")
        DefaultThreadPool += { onCancel() }
    }

    companion object {
        private const val GUI_STEPS = 1000 // 0.1%
        private val LAYOUT = "minosoft:eros/dialog/loading.fxml".toResourceLocation()


        fun Double.steps(): Int {
            return (this * GUI_STEPS).toInt()
        }
    }
}
