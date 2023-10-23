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

package de.bixilon.minosoft.gui.rendering.light.debug

import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.eros.controller.JavaFXWindowController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.rendering.light.Lightmap
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import javafx.fxml.FXML
import javafx.scene.canvas.Canvas

class LightmapDebugWindow(private val lightmap: Lightmap) : JavaFXWindowController() {
    private var show = false
    @FXML private lateinit var canvasFX: Canvas


    public override fun show() {
        if (show) {
            return
        }
        JavaFXUtil.openModalAsync("Lightmap", LAYOUT, controller = this) { super.show(); show = true }
    }

    override fun close() {
        super.close()
        show = false
    }

    private fun _update() {
        if (!show) {
            return
        }
        val buffer = lightmap.buffer.buffer.buffer

        for (sky in 0 until ProtocolDefinition.LIGHT_LEVELS) {
            for (block in 0 until ProtocolDefinition.LIGHT_LEVELS) {
                val offset = ((sky shl 4) or block) * 4
                val color = RGBColor(buffer.get(offset + 0), buffer.get(offset + 1), buffer.get(offset + 2))
                canvasFX.graphicsContext2D.pixelWriter.setArgb(block, sky, color.argb)
            }
        }
    }

    fun update() {
        if (!show) {
            return
        }
        JavaFXUtil.runLater { _update() }
    }


    companion object {
        private val LAYOUT = minosoft("eros/debug/lightmap.fxml")
    }
}
