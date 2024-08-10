/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.eros.education

import de.bixilon.kutil.shutdown.ShutdownManager
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.gui.eros.controller.JavaFXWindowController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import javafx.fxml.FXML
import javafx.scene.control.ComboBox
import javafx.scene.control.Slider
import javafx.scene.control.TextField
import javafx.scene.text.TextFlow
import java.lang.reflect.Method

class EducationUI(
    val session: PlaySession,
) : JavaFXWindowController() {
    @FXML private lateinit var blockX: TextField
    @FXML private lateinit var blockY: TextField
    @FXML private lateinit var blockZ: TextField
    @FXML private lateinit var blockFX: ComboBox<Block>

    @FXML private lateinit var entityFX: ComboBox<Entity>

    @FXML private lateinit var timeStatus: TextFlow
    @FXML private lateinit var timeSpeed: Slider

    @FXML private lateinit var codeStatus: TextFlow

    @FXML private lateinit var functionsFX: ComboBox<Method>

    public override fun show() {
        JavaFXUtil.openModalAsync("Minosoft Education UI", LAYOUT, this) { super.show() }
    }

    override fun postInit() {
        stage.setOnCloseRequest {
            if (closing) return@setOnCloseRequest
            ShutdownManager.shutdown()
        }
    }

    override fun close() {
        super.close()
        ShutdownManager.shutdown()
    }

    fun reset() = Unit
    fun killAll() = Unit
    fun freeze() = Unit

    fun getBlock() = Unit
    fun setBlock() = Unit

    fun kill() = Unit
    fun highlight() = Unit
    fun teleport() = Unit

    fun pause() = Unit
    fun step() = Unit


    fun stop() = Unit
    fun restart() = Unit
    fun reload() = Unit

    fun invoke() = Unit

    companion object {
        val LAYOUT = minosoft("eros/education/education.fxml")
    }
}
