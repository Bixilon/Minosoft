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
import de.bixilon.minosoft.gui.eros.main.play.server.Refreshable
import de.bixilon.minosoft.gui.eros.main.play.server.ServerListController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane

class PlayMainController : EmbeddedJavaFXController<Pane>() {
    @FXML
    private lateinit var playTypeContentFX: Pane

    @FXML
    private lateinit var playTypeListViewFX: ListView<*>

    @FXML
    private lateinit var refreshPaneFX: AnchorPane


    private lateinit var currentController: EmbeddedJavaFXController<*>

    override fun init() {
        currentController = JavaFXUtil.loadEmbeddedController<ServerListController>(ServerListController.LAYOUT)
        playTypeContentFX.children.setAll(currentController.root)

        JavaFXUtil.loadEmbeddedController<ServerTypeCardController>(ServerTypeCardController.LAYOUT).apply {
            refreshPaneFX.children.setAll(root)
            iconFX.iconLiteral = "fas-sync-alt"
            headerFX.text = REFRESH_HEADER
            text1FX.text = REFRESH_TEXT1
            text2FX.text = REFRESH_TEXT2

            root.setOnMouseClicked {
                val currentController = currentController
                if (currentController is Refreshable) {
                    currentController.refresh()
                }
            }
        }
    }

    companion object {
        private val REFRESH_HEADER = "minosoft:server_list.refresh.header".asResourceLocation()
        private val REFRESH_TEXT1 = "minosoft:server_list.refresh.text1".asResourceLocation()
        private val REFRESH_TEXT2 = "minosoft:server_list.refresh.text2".asResourceLocation()
    }
}
