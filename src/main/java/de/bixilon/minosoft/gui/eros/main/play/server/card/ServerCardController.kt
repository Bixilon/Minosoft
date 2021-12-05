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

package de.bixilon.minosoft.gui.eros.main.play.server.card

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.eros.card.AbstractCardController
import de.bixilon.minosoft.gui.eros.card.CardFactory
import de.bixilon.minosoft.gui.eros.main.play.server.ServerListController
import de.bixilon.minosoft.gui.eros.modding.invoker.JavaFXEventInvoker
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.ctext
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.modding.event.events.connection.ConnectionErrorEvent
import de.bixilon.minosoft.modding.event.events.connection.status.ServerStatusReceiveEvent
import de.bixilon.minosoft.modding.event.events.connection.status.StatusConnectionStateChangeEvent
import de.bixilon.minosoft.modding.event.events.connection.status.StatusPongReceiveEvent
import de.bixilon.minosoft.util.KUtil.text
import de.bixilon.minosoft.util.KUtil.thousands
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.text.TextFlow
import java.io.ByteArrayInputStream

class ServerCardController : AbstractCardController<ServerCard>() {
    @FXML private lateinit var faviconFX: ImageView
    @FXML private lateinit var serverNameFX: TextFlow
    @FXML private lateinit var motdFX: TextFlow
    @FXML private lateinit var pingFX: Label
    @FXML private lateinit var playerCountFX: Label
    @FXML private lateinit var serverVersionFX: Label

    var serverList: ServerListController? = null

    var serverCard: ServerCard? = null
        private set

    override fun clear() {
        faviconFX.image = JavaFXUtil.MINOSOFT_LOGO

        serverNameFX.children.clear()
        motdFX.children.clear()
        pingFX.ctext = ""
        playerCountFX.ctext = ""
        serverVersionFX.ctext = ""
    }

    override fun updateItem(item: ServerCard?, empty: Boolean) {
        super.updateItem(item, empty)

        root.isVisible = item != null
        this.serverCard = item
        item ?: return

        serverNameFX.text = item.server.name

        item.unregister()
        item.ping()

        item.favicon?.let { faviconFX.image = it }

        item.statusReceiveInvoker = JavaFXEventInvoker.of<ServerStatusReceiveEvent> {
            if (serverCard != item || it.connection.error != null) {
                // error already occurred, not setting any data
                return@of
            }
            motdFX.text = it.status.motd ?: ChatComponent.EMPTY
            playerCountFX.ctext = "${it.status.usedSlots?.thousands()} / ${it.status.slots?.thousands()}"
            serverVersionFX.ctext = it.connection.serverVersion?.name

            faviconFX.image = it.status.favicon?.let { favicon -> Image(ByteArrayInputStream(favicon)) } ?: JavaFXUtil.MINOSOFT_LOGO

            it.status.favicon?.let { favicon -> item.rawFavicon = favicon }
            serverList?.onPingUpdate(item)
        }

        item.statusUpdateInvoker = JavaFXEventInvoker.of<StatusConnectionStateChangeEvent> {
            if (serverCard != item || it.connection.error != null || it.connection.lastServerStatus != null) {
                // error or motd is already displayed
                return@of
            }
            motdFX.text = ChatComponent.of(Minosoft.LANGUAGE_MANAGER.translate(it.state))
            serverList?.onPingUpdate(item)
        }

        item.statusErrorInvoker = JavaFXEventInvoker.of<ConnectionErrorEvent> {
            if (serverCard != item) {
                return@of
            }
            motdFX.text = it.exception.text
            serverList?.onPingUpdate(item)
        }

        item.pongInvoker = JavaFXEventInvoker.of<StatusPongReceiveEvent> {
            if (serverCard != item || it.connection.error != null) {
                // error already occurred, not setting any data
                return@of
            }
            pingFX.text = "${it.latency} ms"
            serverList?.onPingUpdate(item)
        }
    }

    companion object : CardFactory<ServerCardController> {
        override val LAYOUT: ResourceLocation = "minosoft:eros/main/play/server/server_card.fxml".toResourceLocation()
    }
}
