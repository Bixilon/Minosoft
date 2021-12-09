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
import de.bixilon.minosoft.config.profile.ConnectionProfiles
import de.bixilon.minosoft.config.profile.delegate.watcher.SimpleProfileDelegateWatcher.Companion.profileWatchFX
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.Server
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TranslatableComponents
import de.bixilon.minosoft.gui.eros.Eros
import de.bixilon.minosoft.gui.eros.controller.EmbeddedJavaFXController
import de.bixilon.minosoft.gui.eros.dialog.ServerModifyDialog
import de.bixilon.minosoft.gui.eros.dialog.SimpleErosConfirmationDialog
import de.bixilon.minosoft.gui.eros.dialog.connection.KickDialog
import de.bixilon.minosoft.gui.eros.main.play.server.card.FaviconManager.saveFavicon
import de.bixilon.minosoft.gui.eros.main.play.server.card.ServerCard
import de.bixilon.minosoft.gui.eros.main.play.server.card.ServerCardController
import de.bixilon.minosoft.gui.eros.main.play.server.type.types.ServerType
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
import de.bixilon.minosoft.util.delegate.watcher.entry.ListDelegateWatcher.Companion.watchListFX
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

    var serverType: ServerType? = null
        set(value) {
            check(value != null)
            field = value

            addServerButtonFX.isVisible = !value.readOnly
        }

    override fun init() {
        val erosProfile = ErosProfileManager.selected
        val serverConfig = erosProfile.server.list
        serverConfig::hideOffline.profileWatchFX(this, true) { hideOfflineFX.isSelected = it;refreshList() }
        serverConfig::hideFull.profileWatchFX(this, true) { hideFullFX.isSelected = it;refreshList() }
        serverConfig::hideEmpty.profileWatchFX(this, true) { hideEmptyFX.isSelected = it;refreshList() }

        hideOfflineFX.setOnAction { ErosProfileManager.selected.server.list.hideOffline = hideOfflineFX.isSelected }
        hideFullFX.setOnAction { ErosProfileManager.selected.server.list.hideFull = hideFullFX.isSelected }
        hideEmptyFX.setOnAction { ErosProfileManager.selected.server.list.hideEmpty = hideEmptyFX.isSelected }


        val accountProfile = erosProfile.general.accountProfile
        serverListViewFX.setCellFactory {
            val controller = ServerCardController.build()
            controller.serverList = this

            controller.root.setOnMouseClicked {
                if (it.clickCount != 2) {
                    return@setOnMouseClicked
                }
                val card = controller.item ?: return@setOnMouseClicked
                if (!card.canConnect(accountProfile.selected ?: return@setOnMouseClicked)) {
                    return@setOnMouseClicked
                }

                connect(card)
            }
            return@setCellFactory controller
        }

        serverListViewFX.selectionModel.selectedItemProperty().addListener { _, _, new ->
            setServerInfo(new)
        }
    }

    fun connect(serverCard: ServerCard) {
        val server = serverCard.server
        val ping = serverCard.ping ?: return
        val pingVersion = serverCard.ping?.serverVersion ?: return
        Eros.mainErosController.verifyAccount { account ->
            DefaultThreadPool += {
                val connection = PlayConnection(
                    address = ping.realAddress ?: DNSUtil.getServerAddress(server.address),
                    account = account,
                    version = serverCard.server.forcedVersion ?: pingVersion,
                    profiles = ConnectionProfiles(ErosProfileManager.selected.general.profileOverrides.toMutableMap().apply { putAll(server.profiles) })
                )
                account.connections[server] = connection
                serverCard.connections += connection

                connection.registerEvent(CallbackEventInvoker.of<PlayConnectionStateChangeEvent> { event ->
                    if (event.state.disconnected) {
                        account.connections -= server
                        serverCard.connections -= connection
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

    fun initWatch() {
        serverType!!::servers.watchListFX(this) {
            while (it.next()) {
                for (removed in it.removed) {
                    serverListViewFX.items -= ServerCard.CARDS.remove(removed)
                }
                for (added in it.addedSubList) {
                    updateServer(added)
                }
            }
        }
    }

    @FXML
    fun refreshList() {
        if (serverType == null) {
            return
        }
        val selected = serverListViewFX.selectionModel.selectedItem
        serverListViewFX.items.clear()

        for (server in serverType!!.servers) {
            updateServer(server)
        }

        serverListViewFX.items.contains(selected).decide(selected, null).let {
            serverListViewFX.selectionModel.select(it)

            serverListViewFX.scrollTo(it)
        }
    }

    private fun updateServer(server: Server) {
        val serverType = serverType ?: return
        if (server !in serverType.servers) {
            throw IllegalStateException("Server is not even in list!") // probably forgot to unregister listeners
        }
        val card = ServerCard.CARDS[server] ?: ServerCard(server).apply {
            serverListStatusInvoker = JavaFXEventInvoker.of<StatusConnectionStateChangeEvent>(instantFire = false) { updateServer(server) }
        }
        val wasSelected = serverListViewFX.selectionModel.selectedItem === card
        // Platform.runLater {serverListViewFX.items.remove(card)}

        card.ping?.let {
            if (it.hide) {
                return
            }
        }

        if (!serverListViewFX.items.contains(card)) {
            serverListViewFX.items.add(card)
        }
        // serverListViewFX.items.sortBy { it.server.id } // ToDo (Performance): Do not sort, add before/after other server


        if (wasSelected) {
            serverListViewFX.selectionModel.select(card)
        }
    }


    private fun setServerInfo(serverCard: ServerCard?) {
        if (serverCard == null) {
            serverInfoFX.children.clear()
            return
        }
        val serverType = serverType
        val account = ErosProfileManager.selected.general.accountProfile

        val pane = GridPane()

        AnchorPane.setLeftAnchor(pane, 10.0)
        AnchorPane.setRightAnchor(pane, 10.0)


        GridPane().let {
            var row = 0

            for ((key, property) in SERVER_INFO_PROPERTIES) {
                val propertyValue = property(serverCard) ?: continue

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

            if (!serverType!!.readOnly) {
                it.add(Button("Delete").apply {
                    setOnAction {
                        SimpleErosConfirmationDialog(confirmButtonText = "minosoft:general.delete".toResourceLocation(), description = TranslatableComponents.EROS_DELETE_SERVER_CONFIRM_DESCRIPTION(serverCard.server.name, serverCard.server.address), onConfirm = {
                            serverType.servers -= serverCard.server
                        }).show()
                    }
                }, 0, 0)
                it.add(Button("Edit").apply {
                    setOnAction {
                        val server = serverCard.server
                        ServerModifyDialog(server = server, onUpdate = { name, address, forcedVersion, profiles ->
                            server.name = ChatComponent.of(name)
                            server.forcedVersion = forcedVersion
                            server.profiles = profiles.toMutableMap()
                            if (server.address != address) {
                                server.faviconHash?.let { hash -> server.saveFavicon(null, hash) }

                                server.address = address

                                // disconnect all ping connections, re ping
                                // ToDo: server.connections.clear()

                                serverCard.unregister()
                                serverCard.ping?.disconnect()
                                serverCard.ping = null
                                serverCard.ping()
                            }
                            JavaFXUtil.runLater { refreshList() }
                        }).show()
                    }
                }, 1, 0)
            }

            it.add(Button("Refresh").apply {
                setOnAction {
                    serverCard.ping().ping()
                }
                isDisable = serverCard.ping != null && serverCard.ping?.state != StatusConnectionStates.PING_DONE && serverCard.ping?.state != StatusConnectionStates.ERROR
            }, 3, 0)
            it.add(Button("Connect").apply {
                setOnAction {
                    isDisable = true
                    connect(serverCard)
                }
                val selected = account.selected
                isDisable = selected == null || !serverCard.canConnect(selected)
                // ToDo: Also disable, if currently connecting
            }, 4, 0)


            it.hgap = 5.0
            GridPane.setMargin(it, Insets(20.0, 0.0, 0.0, 0.0))

            pane.add(it, 0, 1)
        }


        serverInfoFX.children.setAll(pane)
    }

    val StatusConnection.hide: Boolean
        get() {
            if (hideOfflineFX.isSelected && error != null) {
                return true
            }

            lastServerStatus?.let { status ->
                val usedSlots = status.usedSlots ?: 0
                val slots = status.slots ?: 0
                if (hideFullFX.isSelected && usedSlots >= slots && slots > 0) {
                    return true
                }

                if (hideEmptyFX.isSelected && usedSlots == 0 && slots > 0) {
                    return true
                }
            }
            return false
        }

    fun onPingUpdate(card: ServerCard) {
        val ping = card.ping ?: return
        if (ping.hide) {
            if (serverListViewFX.selectionModel.selectedItem == card) {
                serverListViewFX.selectionModel.select(null)
            }
            serverListViewFX.items.remove(card)
        }
    }

    @FXML
    fun addServer() {
        ServerModifyDialog(onUpdate = { name, address, forcedVersion, profiles ->
            serverType!!.servers += Server(name = ChatComponent.of(name), address = address, forcedVersion = forcedVersion, profiles = profiles.toMutableMap())
        }).show()
    }

    override fun refresh() {
        serverType!!.refresh(serverListViewFX.items)
    }


    companion object {
        val LAYOUT = "minosoft:eros/main/play/server/server_list.fxml".toResourceLocation()

        private val SERVER_INFO_PROPERTIES: List<Pair<ResourceLocation, (ServerCard) -> Any?>> = listOf(
            "minosoft:server_info.server_name".toResourceLocation() to { it.server.name },
            "minosoft:server_info.server_address".toResourceLocation() to { it.server.address },
            "minosoft:server_info.real_server_address".toResourceLocation() to { it.ping?.realAddress },
            "minosoft:server_info.forced_version".toResourceLocation() to { it.server.forcedVersion },

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
