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

package de.bixilon.minosoft.gui.eros.main.play

import de.bixilon.minosoft.gui.eros.controller.EmbeddedJavaFXController
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import javafx.fxml.FXML
import javafx.scene.layout.HBox
import javafx.scene.text.TextFlow
import org.kordamp.ikonli.javafx.FontIcon

class ServerTypeCardController : EmbeddedJavaFXController<HBox>() {
    @FXML
    lateinit var iconFX: FontIcon

    @FXML
    lateinit var headerFX: TextFlow

    @FXML
    lateinit var text1FX: TextFlow

    @FXML
    lateinit var text2FX: TextFlow


    companion object {
        val LAYOUT = "minosoft:eros/main/play/server_type_card.fxml".asResourceLocation()
    }
}
