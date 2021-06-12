/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.gui.main

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.locale.LocaleManager
import de.bixilon.minosoft.data.locale.Strings
import de.bixilon.minosoft.data.player.tab.PingBars
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.data.registries.versions.Versions
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.modding.event.CallbackEventInvoker
import de.bixilon.minosoft.modding.event.events.ConnectionStateChangeEvent
import de.bixilon.minosoft.modding.event.events.ServerListPongEvent
import de.bixilon.minosoft.modding.event.events.ServerListStatusArriveEvent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.ping.ServerModInfo
import de.bixilon.minosoft.protocol.protocol.ConnectionStates
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.CountUpAndDownLatch
import de.bixilon.minosoft.util.logging.Log
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.control.*
import javafx.scene.image.ImageView
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.paint.Color
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.Modality
import java.net.URL
import java.util.*


class ServerListCell : ListCell<Server?>(), Initializable {
    lateinit var hBox: HBox
    lateinit var root: GridPane
    lateinit var faviconField: ImageView
    lateinit var nameField: TextFlow
    lateinit var motdField: TextFlow
    lateinit var versionField: Label
    lateinit var playersField: Label
    lateinit var brandField: Label
    lateinit var optionsConnect: MenuItem
    lateinit var optionsShowInfo: MenuItem
    lateinit var optionsEdit: MenuItem
    lateinit var optionsRefresh: MenuItem
    lateinit var optionsSessions: MenuItem
    lateinit var optionsDelete: MenuItem
    lateinit var optionsMenu: MenuButton
    lateinit var pingField: Label
    private var server: Server? = null
    private var connectable = true


    var name: ChatComponent = ChatComponent.of("")
        set(value) {
            field = value
            nameField.children.setAll(name.javaFXText)
            for (node in nameField.children) {
                node.style = "-fx-font-size: 15pt ;"
            }
        }

    override fun initialize(url: URL, rb: ResourceBundle?) {
        updateSelected(false)
        graphic = this.root

        // change locale
        optionsConnect.text = LocaleManager.translate(Strings.SERVER_ACTION_CONNECT)
        optionsShowInfo.text = LocaleManager.translate(Strings.SERVER_ACTION_SHOW_INFO)
        optionsEdit.text = LocaleManager.translate(Strings.SERVER_ACTION_EDIT)
        optionsRefresh.text = LocaleManager.translate(Strings.SERVER_ACTION_REFRESH)
        optionsSessions.text = LocaleManager.translate(Strings.SERVER_ACTION_SESSIONS)
        optionsDelete.text = LocaleManager.translate(Strings.SERVER_ACTION_DELETE)
    }

    override fun updateItem(server: Server?, empty: Boolean) {
        super.updateItem(server, empty)

        this.root.isVisible = server != null || !empty
        this.hBox.isVisible = server != null || !empty

        if (empty || server == null) {
            return
        }

        resetCell()

        server.cell = this

        this.server = server

        name = server.name

        faviconField.image = GUITools.getImage(server.favicon) ?: GUITools.MINOSOFT_LOGO

        if (server.isConnected) {
            root.styleClass.add("list-cell-connected")
            optionsSessions.isDisable = false
        } else {
            optionsSessions.isDisable = true
        }
        if (server.lastPing == null) {
            server.ping()
        }

        server.lastPing.registerEvent(CallbackEventInvoker.of<ServerListStatusArriveEvent> {
            Platform.runLater {
                val ping = it.serverListPing

                if (this.server != server) {
                    // this cell does not belong here anymore
                    return@runLater
                }
                resetCell()
                if (server.isConnected) {
                    root.styleClass.add("list-cell-connected")
                }
                if (ping == null) {
                    this.playersField.text = ""
                    this.versionField.text = LocaleManager.translate(Strings.OFFLINE)
                    this.versionField.styleClass.add("version-error")
                    setErrorMotd(server.lastPing.error.toString())
                    this.optionsConnect.isDisable = true
                    this.connectable = false
                    return@runLater
                }

                this.playersField.text = LocaleManager.translate(Strings.SERVER_INFO_SLOTS_PLAYERS_ONLINE, ping.playerOnline, ping.maxPlayers)

                val serverVersion: Version

                if (server.desiredVersionId == ProtocolDefinition.QUERY_PROTOCOL_VERSION_ID) {
                    serverVersion = Versions.getVersionByProtocolId(ping.protocolId)
                } else {
                    serverVersion = Versions.getVersionById(server.desiredVersionId)
                    versionField.style = "-fx-text-fill: -secondary-light-light-color;"
                }
                if (serverVersion == null) {
                    versionField.text = ping.serverBrand
                    versionField.style = "-fx-text-fill: red;"
                    optionsConnect.isDisable = true
                    connectable = false
                } else {
                    versionField.text = serverVersion.versionName
                    optionsConnect.isDisable = false
                    connectable = true
                }

                ping.getServerModInfo<ServerModInfo>().let { modInfo ->
                    brandField.text = modInfo.brand
                    brandField.tooltip = Tooltip(modInfo.info)
                }
                motdField.children.setAll(ping.motd.javaFXText)

                ping.favicon?.let {
                    this.faviconField.image = GUITools.getImage(ping.favicon)
                    if (!Arrays.equals(ping.favicon, server.favicon)) {
                        server.favicon = ping.favicon
                        server.saveToConfig()
                    }
                }

                if (server.isTemporary) {
                    this.optionsEdit.isDisable = true
                    this.optionsDelete.isDisable = true
                }

                server.lastPing.error?.let { exception ->
                    // connection failed because of an error in minosoft, but ping was okay
                    this.versionField.style = "-fx-text-fill: red;"
                    this.optionsConnect.isDisable = true
                    this.connectable = false
                    setErrorMotd(String.format("%s: %s", exception::class.java.canonicalName, exception.message))
                }

                server.lastPing.registerEvent(CallbackEventInvoker.of<ServerListPongEvent> {
                    Platform.runLater {
                        this.pingField.text = "${it.latency}ms"
                        when (PingBars.byPing(it.latency)) {
                            PingBars.BARS_5 -> this.pingField.styleClass.add("ping-5-bars")
                            PingBars.BARS_4 -> this.pingField.styleClass.add("ping-4-bars")
                            PingBars.BARS_3 -> this.pingField.styleClass.add("ping-3-bars")
                            PingBars.BARS_2 -> this.pingField.styleClass.add("ping-2-bars")
                            PingBars.BARS_1 -> this.pingField.styleClass.add("ping-1-bars")
                            PingBars.NO_CONNECTION -> this.pingField.styleClass.add("ping-no-connection")
                        }
                    }
                })
            }
        })
    }

    private fun resetCell() {
        // clear all cells
        style = null
        this.root.styleClass.removeAll("list-cell-connected")
        this.root.styleClass.removeAll("list-cell-connecting")
        motdField.children.clear()
        brandField.text = ""
        brandField.tooltip = null
        motdField.style = null
        versionField.text = LocaleManager.translate(Strings.CONNECTING)
        versionField.styleClass.removeAll("version-error")
        versionField.style = null
        playersField.text = ""
        pingField.text = ""
        pingField.styleClass.removeIf { s: String -> s.startsWith("ping") }
        optionsConnect.isDisable = true
        optionsEdit.isDisable = false
        optionsDelete.isDisable = false
    }

    private fun setErrorMotd(message: String) {
        val text = Text(message)
        text.fill = Color.RED
        motdField.children.setAll(text)
    }

    fun connect() {
        val server = this.server!!
        if (!this.connectable || server.lastPing == null) {
            return
        }
        if (server.isConnected) {
            return
        }
        this.root.styleClass.add("list-cell-connecting")
        Minosoft.THREAD_POOL.execute {
            val version: Version = if (server.desiredVersionId == ProtocolDefinition.QUERY_PROTOCOL_VERSION_ID) {
                server.lastPing.serverVersion!!
            } else {
                Versions.getVersionById(server.desiredVersionId)
            }
            val connection = PlayConnection(server.lastPing.realAddress, Minosoft.getConfig().config.account.entries[Minosoft.getConfig().config.account.selected]!!, version)
            server.addConnection(connection)
            Platform.runLater { optionsConnect.isDisable = true }
            // ToDo: show progress dialog
            connection.registerEvent(CallbackEventInvoker.of<ConnectionStateChangeEvent> {
                if (!server.connections.contains(connection)) {
                    // the card got recycled
                    return@of
                }
                Platform.runLater {
                    this.root.styleClass.removeAll("list-cell-connecting")
                    this.root.styleClass.removeAll("list-cell-connected")
                    this.root.styleClass.removeAll("list-cell-disconnecting")
                    this.root.styleClass.removeAll("list-cell-failed")
                    this.root.styleClass.add(when (connection.connectionState) {
                        ConnectionStates.CONNECTING, ConnectionStates.HANDSHAKING, ConnectionStates.LOGIN -> "list-cell-connecting"
                        ConnectionStates.PLAY -> "list-cell-connected"
                        else -> ""
                    })
                    if (connection.error != null) {
                        this.root.styleClass.add("list-cell-failed")
                    }
                    if (connection.connectionState.connected) {
                        optionsConnect.isDisable = Minosoft.getConfig().config.account.selected == connection.account.id
                        optionsSessions.isDisable = false
                        return@runLater
                    }
                    if (server.isConnected) {
                        optionsSessions.isDisable = false
                        optionsConnect.isDisable = false
                        return@runLater
                    }
                    optionsConnect.isDisable = false
                    optionsSessions.isDisable = true
                }
            })
            connection.connect(CountUpAndDownLatch(1))
        }
    }

    fun showInfo() {
        TODO("Not implemented yet!")
    }

    fun clicked(event: MouseEvent) {
        when (event.button) {
            MouseButton.PRIMARY -> {
                if (event.clickCount == 2) {
                    connect()
                }
            }
            MouseButton.SECONDARY -> this.optionsMenu.fire()
            MouseButton.MIDDLE -> {
                SERVER_LIST_VIEW.selectionModel.select(this.server)
                editServer()
            }
            else -> {
            }
        }
    }

    fun delete() {
        val server = this.server!!
        if (server.isTemporary) {
            return
        }
        for (connection in server.connections) {
            connection.disconnect()
        }
        server.delete()
        Log.info("Deleted server (name=\"${server.name}\", address=\"${server.address}\")")
        SERVER_LIST_VIEW.items.remove(server)
    }

    fun refresh() {
        val server = this.server!!
        if (server.lastPing == null) {
            // server was not pinged, don't even try, only costs memory and cpu
            return
        }
        Log.info("Refreshing server status (serverName=\"${server.name}\", address=\"${server.address}\")")
        resetCell()
        server.ping()
    }

    fun editServer() {
        MainWindow.addOrEditServer(server)
    }

    fun manageSessions() {
        val sessionsWindow = GUITools.showPane<SessionsWindow>(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "layout/sessions.fxml"), Modality.APPLICATION_MODAL, LocaleManager.translate(Strings.SESSIONS_DIALOG_TITLE, server!!.name.message))
        sessionsWindow.setServer(server)
    }

    companion object {
        @JvmField
        val SERVER_LIST_VIEW = ListView<Server>()

        @JvmStatic
        fun newInstance(): ServerListCell {
            val loader = FXMLLoader(Minosoft.MINOSOFT_ASSETS_MANAGER.getAssetURL(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "layout/cells/server.fxml")))

            loader.load<Any>()
            return loader.getController()
        }
    }
}
