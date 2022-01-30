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

package de.bixilon.minosoft.gui.eros.main.about

import de.bixilon.minosoft.gui.eros.controller.EmbeddedJavaFXController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.fxml.FXML
import javafx.scene.control.TextArea
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.text.TextFlow

class AboutController : EmbeddedJavaFXController<HBox>() {
    @FXML private lateinit var minosoftLogoFX: ImageView
    @FXML private lateinit var bixilonLogoFX: Pane

    @FXML private lateinit var versionStringFX: TextFlow
    @FXML private lateinit var aboutTextFX: TextFlow
    @FXML private lateinit var copyrightFX: TextArea


    override fun init() {
        minosoftLogoFX.image = JavaFXUtil.MINOSOFT_LOGO

        bixilonLogoFX.children.setAll(JavaFXUtil.BIXILON_LOGO)

        versionStringFX.text = RunConfiguration.VERSION_STRING
        aboutTextFX.text =
            """
No clue what to put here :(
"""
    }

    companion object {
        val LAYOUT = "minosoft:eros/main/about/about.fxml".toResourceLocation()
    }
}
