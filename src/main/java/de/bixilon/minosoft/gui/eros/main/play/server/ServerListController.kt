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
import de.bixilon.minosoft.data.text.TranslatableComponents
import de.bixilon.minosoft.gui.eros.Eros
import de.bixilon.minosoft.gui.eros.controller.EmbeddedJavaFXController
import de.bixilon.minosoft.gui.eros.dialog.SimpleErosConfirmationDialog
import de.bixilon.minosoft.gui.eros.dialog.UpdateServerDialog
import de.bixilon.minosoft.gui.eros.dialog.connection.KickDialog
import de.bixilon.minosoft.gui.eros.main.play.server.card.ServerCard
import de.bixilon.minosoft.gui.eros.main.play.server.card.ServerCardController
import de.bixilon.minosoft.gui.eros.modding.invoker.JavaFXEventInvoker
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.modding.event.events.KickEvent
import de.bixilon.minosoft.modding.event.events.LoginKickEvent
import de.bixilon.minosoft.modding.event.events.connection.play.PlayConnectionStateChangeEvent
import de.bixilon.minosoft.modding.event.events.connection.status.StatusConnectionStateChangeEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates.Companion.disconnected
import de.bixilon.minosoft.protocol.network.connection.status.StatusConnection
import de.bixilon.minosoft.protocol.network.connection.status.StatusConnectionStates
import de.bixilon.minosoft.util.DNSUtil
import de.bixilon.minosoft.util.KUtil.decide
import de.bixilon.minosoft.util.KUtil.thousands
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.task.pool.DefaultThreadPool
import javafx.fxml.FXML
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ListView
import javafx.scene.layout.*


class ServerListController : EmbeddedJavaFXController<Pane>(), Refreshable {
    @FXML private lateinit var hideOfflineFX: CheckBox

    @FXML private lateinit var hideFullFX: CheckBox

    @FXML private lateinit var hideEmptyFX: CheckBox


    @FXML private lateinit var addServerButtonFX: Button

    @FXML private lateinit var serverListViewFX: ListView<ServerCard>

    @FXML private lateinit var serverInfoFX: AnchorPane

    var customRefresh: (() -> Unit)? = null

    var servers: MutableCollection<Server> = mutableListOf()

    var readOnly: Boolean = false
        set(value) {
            field = value
            addServerButtonFX.isVisible = !value
        }

    override fun init() {
        serverListViewFX.setCellFactory {
            val controller = ServerCardController.build()

            controller.root.setOnMouseClicked {
                if (it.clickCount != 2) {
                    return@setOnMouseClicked
                }
                val server = controller.lastServerCard?.server ?: return@setOnMouseClicked
                if (!server.canConnect) {
                    return@setOnMouseClicked
                }

                connect(server)
            }
            return@setCellFactory controller
        }

        refreshList()

        serverListViewFX.selectionModel.selectedItemProperty().addListener { _, old, new ->
            setServerInfo(new)
        }
    }

    fun connect(server: Server, ping: StatusConnection? = server.ping) {
        val pingVersion = ping?.serverVersion ?: return
        Eros.mainErosController.verifyAccount { account ->
            DefaultThreadPool += {
                val connection = PlayConnection(
                    address = server.ping?.realAddress ?: DNSUtil.getServerAddress(server.address),
                    account = account,
                    version = server.forcedVersion ?: pingVersion,
                )
                account.connections[server] = connection
                server.connections += connection

                connection.registerEvent(CallbackEventInvoker.of<PlayConnectionStateChangeEvent> { event ->
                    if (event.state.disconnected) {
                        account.connections -= server
                        server.connections -= connection
                    }
                    JavaFXUtil.runLater { updateServer(server) }
                })

                connection.registerEvent(JavaFXEventInvoker.of<KickEvent> { event ->
                    KickDialog(
                        title = "minosoft:connection.kick.title".toResourceLocation(),
                        header = "minosoft:connection.kick.header".toResourceLocation(),
                        description = TranslatableComponents.CONNECTION_KICK_DESCRIPTION(server, account),
                        reason = event.reason,
                    ).show()
                })
                connection.registerEvent(JavaFXEventInvoker.of<LoginKickEvent> { event ->
                    KickDialog(
                        title = "minosoft:connection.login_kick.title".toResourceLocation(),
                        header = "minosoft:connection.login_kick.header".toResourceLocation(),
                        description = TranslatableComponents.CONNECTION_LOGIN_KICK_DESCRIPTION(server, account),
                        reason = event.reason,
                    ).show()
                })
                connection.connect()
            }
        }
    }

    override fun postInit() {
        root.setOnKeyPressed { serverListViewFX.selectionModel.select(null) } // ToDo: Only on escape; not working
    }

    @FXML
    fun refreshList() {
        val selected = serverListViewFX.selectionModel.selectedItem
        serverListViewFX.items.clear()

        for (server in servers) {
            updateServer(server)
        }

        serverListViewFX.items.contains(selected).decide(selected, null).let {
            serverListViewFX.selectionModel.select(it)

            serverListViewFX.scrollTo(it)
        }
    }

    private fun updateServer(server: Server) {
        val card = server.card ?: let {
            val card = ServerCard(server)
            card.serverListStatusInvoker = JavaFXEventInvoker.of<StatusConnectionStateChangeEvent>(instantFire = false) { updateServer(server) }
            card
        }
        val wasSelected = serverListViewFX.selectionModel.selectedItem === card
        // Platform.runLater {serverListViewFX.items.remove(card)}

        server.ping?.let {
            if (hideOfflineFX.isSelected && it.error != null) {
                return
            }

            it.lastServerStatus?.let { status ->
                val usedSlots = status.usedSlots ?: 0
                val slots = status.slots ?: 0
                if (hideFullFX.isSelected && usedSlots >= slots && slots > 0) {
                    return
                }

                if (hideEmptyFX.isSelected && usedSlots == 0 && slots > 0) {
                    return
                }
            }
        }

        if (!serverListViewFX.items.contains(card)) {
            serverListViewFX.items.add(card)
        }
        serverListViewFX.items.sortBy { it.server.id } // ToDo (Performance): Do not sort, add before/after other server


        if (wasSelected) {
            serverListViewFX.selectionModel.select(card)
        }
    }


    private fun setServerInfo(serverCard: ServerCard?) {
        if (serverCard == null) {
            serverInfoFX.children.clear()
            return
        }

        val ping = serverCard.server.ping

        val pane = GridPane()

        AnchorPane.setLeftAnchor(pane, 10.0)
        AnchorPane.setRightAnchor(pane, 10.0)


        GridPane().let {
            var row = 0

            for ((key, property) in SERVER_INFO_PROPERTIES) {
                val propertyValue = property(serverCard.server) ?: continue

                it.add(Minosoft.LANGUAGE_MANAGER.translate(key).textFlow, 0, row)
                it.add(ChatComponent.of(propertyValue).textFlow, 1, row++)
            }

            it.columnConstraints += ColumnConstraints(10.0, 180.0, 250.0)
            it.columnConstraints += ColumnConstraints(10.0, 200.0, 300.0)
            it.hgap = 10.0
            it.vgap = 5.0

            pane.add(it, 0, 0)
        }

        GridPane().let {
            it.columnConstraints += ColumnConstraints()
            it.columnConstraints += ColumnConstraints()
            it.columnConstraints += ColumnConstraints(0.0, -1.0, Double.POSITIVE_INFINITY, Priority.ALWAYS, HPos.LEFT, true)

            if (!readOnly) {
                it.add(Button("Delete").apply {
                    setOnAction {
                        SimpleErosConfirmationDialog(
                            confirmButtonText = "minosoft:general.delete".toResourceLocation(),
                            description = TranslatableComponents.EROS_DELETE_SERVER_CONFIRM_DESCRIPTION(serverCard.server.name, serverCard.server.address),
                            onConfirm = {
                                Minosoft.config.config.server.entries.remove(serverCard.server.id)
                                Minosoft.config.saveToFile()
                                JavaFXUtil.runLater { refreshList() }
                            }
                        ).show()
                    }
                }, 1, 0)
                it.add(Button("Edit").apply {
                    setOnAction {
                        val server = serverCard.server
                        UpdateServerDialog(server = server, onUpdate = { name, address, forcedVersion ->
                            server.name = ChatComponent.of(name)
                            server.forcedVersion = forcedVersion
                            if (server.address != address) {
                                server.favicon = null

                                server.address = address

                                // disconnect all ping connections, re ping
                                // ToDo: server.connections.clear()

                                serverCard.unregister()
                                server.ping?.disconnect()
                                server.ping = null
                                server.ping()
                            }
                            Minosoft.config.saveToFile()
                            JavaFXUtil.runLater { refreshList() }
                        }).show()
                    }
                }, 2, 0)
            }

            it.add(Button("Refresh").apply {
                setOnAction {
                    serverCard.server.ping().ping()
                }
                isDisable = serverCard.server.ping != null && serverCard.server.ping?.state != StatusConnectionStates.PING_DONE && serverCard.server.ping?.state != StatusConnectionStates.ERROR
            }, 3, 0)
            it.add(Button("Connect").apply {
                setOnAction {
                    isDisable = true
                    connect(serverCard.server, ping)
                }
                isDisable = !serverCard.server.canConnect
                // ToDo: Also disable, if currently connecting
            }, 4, 0)


            it.hgap = 5.0
            GridPane.setMargin(it, Insets(20.0, 0.0, 0.0, 0.0))

            pane.add(it, 0, 1)
        }


        serverInfoFX.children.setAll(pane)
    }

    @FXML
    fun addServer() {
        UpdateServerDialog(onUpdate = { name, address, forcedVersion ->
            val server = Server(name = ChatComponent.of(name), address = address, forcedVersion = forcedVersion)
            Minosoft.config.config.server.entries[server.id] = server // ToDo
            Minosoft.config.saveToFile()
            JavaFXUtil.runLater { refreshList() }
        }).show()
    }

    override fun refresh() {
        customRefresh?.let {
            it()
            return
        }

        for (serverCard in serverListViewFX.items) {
            serverCard.server.ping?.let {
                if (it.state != StatusConnectionStates.PING_DONE && it.state != StatusConnectionStates.ERROR) {
                    return@let
                }
                it.ping()
            }
        }
    }


    companion object {
        val LAYOUT = "minosoft:eros/main/play/server/server_list.fxml".toResourceLocation()

        private val SERVER_INFO_PROPERTIES: List<Pair<ResourceLocation, (server: Server) -> Any?>> = listOf(
            "minosoft:server_info.server_name".toResourceLocation() to { it.name },
            "minosoft:server_info.server_address".toResourceLocation() to { it.address },
            "minosoft:server_info.real_server_address".toResourceLocation() to { it.ping?.realAddress },
            "minosoft:server_info.forced_version".toResourceLocation() to { it.forcedVersion },

            "minosoft:general.empty".toResourceLocation() to { " " },

            "minosoft:server_info.remote_version".toResourceLocation() to { it.ping?.serverVersion ?: "unknown" },
            "minosoft:server_info.remote_brand".toResourceLocation() to { it.ping?.lastServerStatus?.serverBrand },
            "minosoft:server_info.players_online".toResourceLocation() to { it.ping?.lastServerStatus?.let { status -> "${status.usedSlots?.thousands()} / ${status.slots?.thousands()}" } },
            "minosoft:server_info.ping".toResourceLocation() to { it.ping?.lastPongEvent?.let { pong -> "${pong.latency} ms" } },


            "minosoft:general.empty".toResourceLocation() to { " " },

            "minosoft:server_info.active_connections".toResourceLocation() to { it.connections.size },
        )
    }
}
