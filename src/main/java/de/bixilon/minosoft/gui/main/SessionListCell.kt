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
import de.bixilon.minosoft.data.language.deprecated.DLocaleManager
import de.bixilon.minosoft.data.language.deprecated.Strings
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.modding.event.CallbackEventInvoker
import de.bixilon.minosoft.modding.event.events.ConnectionStateChangeEvent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.fxml.Initializable
import javafx.scene.control.Label
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.MenuItem
import javafx.scene.layout.AnchorPane
import javafx.stage.Stage
import java.net.URL
import java.util.*

class SessionListCell : ListCell<PlayConnection?>(), Initializable {
    lateinit var account: Label
    lateinit var connectionId: Label
    lateinit var optionsDisconnect: MenuItem
    lateinit var root: AnchorPane
    private var connection: PlayConnection? = null

    override fun initialize(url: URL, rb: ResourceBundle?) {
        updateSelected(false)
        graphic = this.root
        optionsDisconnect.text = DLocaleManager.translate(Strings.SESSIONS_ACTION_DISCONNECT)
    }

    override fun updateItem(connection: PlayConnection?, empty: Boolean) {
        super.updateItem(connection, empty)
        this.root.isVisible = !empty
        if (empty) {
            return
        }
        if (connection == null) {
            return
        }
        if (connection == this.connection) {
            return
        }
        style = null
        this.connection = connection
        connection.registerEvent(CallbackEventInvoker.of<ConnectionStateChangeEvent> {
            handleConnectionCallback(it)
        })
        connectionId.text = String.format("#%d", connection.connectionId)
        account.text = connection.account.username
    }

    private fun handleConnectionCallback(event: ConnectionStateChangeEvent) {
        val connection = event.connection as PlayConnection
        if (this.connection != connection) {
            // the card got recycled
            return
        }
        if (!connection.connectionState.connected) {
            Platform.runLater {
                CONNECTION_LIST_VIEW.items.remove(connection)
                if (CONNECTION_LIST_VIEW.items.isEmpty()) {
                    (this.root.scene.window as Stage).close()
                }
            }
        }
    }

    fun disconnect() {
        style = "-fx-background-color: indianred"
        connection!!.disconnect()
    }

    companion object {
        @JvmField
        val CONNECTION_LIST_VIEW = ListView<PlayConnection>()

        @JvmStatic
        fun newInstance(): SessionListCell? {
            val loader = FXMLLoader(Minosoft.MINOSOFT_ASSETS_MANAGER.getAssetURL(ResourceLocation(ProtocolDefinition.MINOSOFT_NAMESPACE, "layout/cells/session.fxml")))
            loader.load<Any>()
            return loader.getController()
        }
    }
}
