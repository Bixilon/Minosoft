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

package de.bixilon.minosoft.gui.eros.main

import de.bixilon.minosoft.gui.eros.controller.JavaFXWindowController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.util.GitInfo
import de.bixilon.minosoft.util.KUtil.decide
import javafx.fxml.FXML
import javafx.scene.image.ImageView
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.text.Text
import org.kordamp.ikonli.javafx.FontIcon


class MainErosController : JavaFXWindowController() {
    @FXML
    private lateinit var logoFX: ImageView

    @FXML
    private lateinit var versionTextFX: Text

    @FXML
    private lateinit var playIconFX: FontIcon

    @FXML
    private lateinit var settingsIconFX: FontIcon

    @FXML
    private lateinit var helpIconFX: FontIcon

    @FXML
    private lateinit var aboutIconFX: FontIcon

    @FXML
    private lateinit var exitIconFX: FontIcon

    @FXML
    private lateinit var contentFX: HBox


    private lateinit var icons: List<FontIcon>


    fun select(iconToSelect: FontIcon) {
        for (icon in icons) {
            if (icon === iconToSelect) {
                continue
            }
            icon.isDisable = true
            icon.iconColor = Color.GRAY
        }
        iconToSelect.isDisable = false
        iconToSelect.iconColor = Color.BLACK
    }

    override fun init() {
        logoFX.image = JavaFXUtil.MINOSOFT_LOGO
        versionTextFX.text = "Minosoft " + GitInfo.IS_INITIALIZED.decide(GitInfo.GIT_COMMIT_ID, "v?")
        icons = listOf(playIconFX, settingsIconFX, helpIconFX, aboutIconFX, exitIconFX)

        select(playIconFX)
    }
}
