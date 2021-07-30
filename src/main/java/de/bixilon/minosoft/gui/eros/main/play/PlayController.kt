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

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.gui.eros.controller.EmbeddedJavaFXController
import de.bixilon.minosoft.gui.eros.main.play.server.Refreshable
import de.bixilon.minosoft.gui.eros.main.play.server.ServerListController
import de.bixilon.minosoft.gui.eros.main.play.server.type.ServerTypeCardController
import de.bixilon.minosoft.gui.eros.modding.events.ErosControllerTerminateEvent
import de.bixilon.minosoft.gui.eros.modding.invoker.JavaFXEventInvoker
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.modding.event.events.LANServerDiscoverEvent
import de.bixilon.minosoft.modding.event.invoker.EventInvoker
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.protocol.protocol.LANServerListener
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.layout.GridPane
import javafx.scene.layout.Pane
import javafx.scene.text.TextFlow

class PlayController : EmbeddedJavaFXController<Pane>() {
    @FXML private lateinit var playTypeContentFX: Pane

    @FXML private lateinit var playTypeListViewFX: ListView<ServerTypes>

    @FXML private lateinit var refreshPaneFX: GridPane

    @FXML private lateinit var refreshHeaderFX: TextFlow

    @FXML private lateinit var refreshText1FX: TextFlow

    @FXML private lateinit var refreshText2FX: TextFlow


    private lateinit var currentController: EmbeddedJavaFXController<*>

    override fun init() {
        playTypeListViewFX.setCellFactory { ServerTypeCardController.build() }
        for (type in ServerTypes.VALUES) {
            if (!type.active) {
                continue
            }
            playTypeListViewFX.items += type
        }
        // ToDo
        check(playTypeListViewFX.items.size > 0)


        playTypeListViewFX.selectionModel.selectedItemProperty().addListener { _, _, new ->
            if (this::currentController.isInitialized) {
                currentController.terminate()
            }
            currentController = when (new!!) {
                ServerTypes.CUSTOM -> JavaFXUtil.loadEmbeddedController<ServerListController>(ServerListController.LAYOUT).apply {
                    servers = Minosoft.config.config.server.entries.values
                    refreshList()
                }
                ServerTypes.LAN -> JavaFXUtil.loadEmbeddedController<ServerListController>(ServerListController.LAYOUT).apply {
                    val events: MutableList<EventInvoker> = mutableListOf()
                    events += GlobalEventMaster.registerEvent(JavaFXEventInvoker.of<LANServerDiscoverEvent> { refreshList() })
                    readOnly = true
                    customRefresh = {
                        LANServerListener.clear()
                        refreshList()
                    }

                    GlobalEventMaster.registerEvent(JavaFXEventInvoker.of<ErosControllerTerminateEvent>(oneShot = true) {
                        if (it.controller != this) {
                            return@of
                        }

                        GlobalEventMaster.unregisterEvents(*events.toTypedArray())
                        LANServerListener.clear()
                    })

                    LANServerListener.clear()
                    servers = LANServerListener.SERVERS.values
                    refreshList()
                }
            }
            playTypeContentFX.children.setAll(currentController.root)
        }

        playTypeListViewFX.selectionModel.select(0)

        refreshHeaderFX.text = REFRESH_HEADER
        refreshText1FX.text = REFRESH_TEXT1
        refreshText2FX.text = REFRESH_TEXT2
        refreshPaneFX.setOnMouseClicked {
            val currentController = currentController
            if (currentController is Refreshable) {
                currentController.refresh()
            }
        }
    }

    companion object {
        val LAYOUT = "minosoft:eros/main/play/play.fxml".asResourceLocation()
        private val CUSTOM_SERVER_TYPE = "minosoft:server_type.custom".asResourceLocation()
        private val LAN_SERVER_TYPE = "minosoft:server_type.lan".asResourceLocation()
        private val REFRESH_HEADER = "minosoft:server_list.refresh.header".asResourceLocation()
        private val REFRESH_TEXT1 = "minosoft:server_list.refresh.text1".asResourceLocation()
        private val REFRESH_TEXT2 = "minosoft:server_list.refresh.text2".asResourceLocation()
    }
}
