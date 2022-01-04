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

package de.bixilon.minosoft.gui.eros.main.play.server.type

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.gui.eros.card.AbstractCardController
import de.bixilon.minosoft.gui.eros.card.CardFactory
import de.bixilon.minosoft.gui.eros.main.play.server.type.types.ServerType
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.delegate.JavaFXDelegate.observeListFX
import javafx.fxml.FXML
import javafx.scene.text.TextFlow
import org.kordamp.ikonli.javafx.FontIcon

class ServerTypeCardController : AbstractCardController<ServerType>() {
    @FXML private lateinit var iconFX: FontIcon
    @FXML private lateinit var headerFX: TextFlow
    @FXML private lateinit var textFX: TextFlow

    override fun updateItem(item: ServerType?, empty: Boolean) {
        val previous = this.item
        super.updateItem(item, empty)
        iconFX.isVisible = !empty
        item ?: return
        if (previous === item) {
            return
        }



        iconFX.iconCode = item.icon
        headerFX.text = Minosoft.LANGUAGE_MANAGER.translate(item)

        recalculate(item)
        item::servers.observeListFX(this) { recalculate(item) }
    }

    private fun recalculate(item: ServerType) {
        textFX.text = "${item.servers.size} servers"
    }


    override fun clear() {
        iconFX.isVisible = false
        headerFX.children.clear()
        textFX.children.clear()
    }

    companion object : CardFactory<ServerTypeCardController> {
        override val LAYOUT = "minosoft:eros/main/play/server_type_card.fxml".toResourceLocation()
    }
}
