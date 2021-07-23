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

package de.bixilon.minosoft.gui.eros.main.play.server

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.server.Server
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.eros.controller.EmbeddedJavaFXController
import de.bixilon.minosoft.gui.eros.main.play.server.card.ServerCard
import de.bixilon.minosoft.gui.eros.main.play.server.card.ServerCardController
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import javafx.fxml.FXML
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ListView
import javafx.scene.layout.*


class ServerListController : EmbeddedJavaFXController<Pane>() {
    @FXML
    private lateinit var hideOfflineFX: CheckBox

    @FXML
    private lateinit var hideFullFX: CheckBox

    @FXML
    private lateinit var hideEmptyFX: CheckBox


    @FXML
    private lateinit var addServerButtonFX: Button

    @FXML
    private lateinit var serverListViewFX: ListView<ServerCard>

    @FXML
    private lateinit var serverInfoFX: AnchorPane


    override fun init() {
        serverListViewFX.setCellFactory { ServerCardController.build() }

        refresh()

        serverListViewFX.selectionModel.selectedItemProperty().addListener { _, _, new ->
            setServerInfo(new)
        }
    }

    @FXML
    fun refresh() {
        serverListViewFX.items.clear()

        for (server in Minosoft.config.config.server.entries.values) {
            // ToDo: Check
            serverListViewFX.items += ServerCard(server)
        }
    }


    private fun setServerInfo(serverCard: ServerCard?) {
        if (serverCard == null) {
            serverInfoFX.children.clear()
            return
        }


        val pane = GridPane()

        AnchorPane.setLeftAnchor(pane, 10.0)
        AnchorPane.setRightAnchor(pane, 10.0)


        GridPane().let {
            var row = 0

            for ((key, property) in SERVER_INFO_PROPERTIES) {
                it.add(Minosoft.LANGUAGE_MANAGER.translate(key).textFlow, 0, row)
                it.add(ChatComponent.of(property(serverCard.server)).textFlow, 1, row++)
            }

            it.columnConstraints += ColumnConstraints(10.0, 100.0, 150.0)
            it.columnConstraints += ColumnConstraints(10.0, 150.0, 200.0)
            it.hgap = 10.0
            it.vgap = 5.0

            pane.add(it, 0, 0)
        }

        GridPane().let {
            it.columnConstraints += ColumnConstraints(0.0, -1.0, Double.POSITIVE_INFINITY, Priority.ALWAYS, HPos.LEFT, true)

            it.add(Button("Delete"), 1, 0)
            it.add(Button("Edit"), 2, 0)
            it.add(Button("Connect"), 3, 0)


            it.hgap = 5.0
            GridPane.setMargin(it, Insets(20.0, 0.0, 0.0, 0.0))

            pane.add(it, 0, 1)
        }


        serverInfoFX.children.setAll(pane)
    }


    private companion object {
        private val SERVER_INFO_PROPERTIES: Map<ResourceLocation, (server: Server) -> Any> = mapOf(
            "minosoft:server_name".asResourceLocation() to { it.name },
            "minosoft:server_address".asResourceLocation() to { it.address },
        )
    }
}
