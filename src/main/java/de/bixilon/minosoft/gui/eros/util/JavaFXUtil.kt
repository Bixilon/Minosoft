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

package de.bixilon.minosoft.gui.eros.util

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.eros.controller.JavaFXController
import de.bixilon.minosoft.gui.eros.controller.JavaFXWindowController
import javafx.application.HostServices
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.image.Image
import javafx.stage.Modality
import javafx.stage.Stage

object JavaFXUtil {
    lateinit var MINOSOFT_LOGO: Image
    lateinit var HOST_SERVICES: HostServices

    fun <T : JavaFXController> openModal(title: ResourceLocation, layout: ResourceLocation, modality: Modality = Modality.WINDOW_MODAL): T {
        val fxmlLoader = FXMLLoader()
        val parent = fxmlLoader.load<Parent>(Minosoft.MINOSOFT_ASSETS_MANAGER.readAssetAsStream(layout))
        val stage = Stage()
        stage.initModality(modality)
        stage.title = Minosoft.LANGUAGE_MANAGER.translate(title).message
        stage.scene = Scene(parent)

        val controller = fxmlLoader.getController<T>()

        if (controller is JavaFXWindowController) {
            controller.stage = stage
        }

        return controller
    }
}
