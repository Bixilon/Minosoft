/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.eros.main.account

import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.TranslatableComponents
import de.bixilon.minosoft.gui.eros.card.AbstractCardController
import de.bixilon.minosoft.gui.eros.card.CardFactory
import de.bixilon.minosoft.gui.eros.util.JavaFXAccountUtil.avatar
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.PixelImageView
import javafx.fxml.FXML
import javafx.scene.text.TextFlow

class AccountCardController : AbstractCardController<Account>() {
    @FXML private lateinit var avatarFX: PixelImageView
    @FXML private lateinit var connectionCountFX: TextFlow
    @FXML private lateinit var stateFX: TextFlow
    @FXML private lateinit var accountNameFX: TextFlow


    override fun clear() {
        avatarFX.image = JavaFXUtil.MINOSOFT_LOGO
        connectionCountFX.children.clear()
        stateFX.children.clear()
        accountNameFX.children.clear()
    }

    override fun updateItem(item: Account?, empty: Boolean) {
        val previous = this.item
        super.updateItem(item, empty)
        root.isVisible = !empty
        item ?: return
        if (previous === item) {
            return
        }

        avatarFX.image = item.avatar
        accountNameFX.text = item.username
        stateFX.text = item.state
        connectionCountFX.text = TranslatableComponents.ACCOUNT_CARD_CONNECTION_COUNT(item.connections.size)
    }

    companion object : CardFactory<AccountCardController> {
        override val LAYOUT: ResourceLocation = "minosoft:eros/main/account/account_card.fxml".toResourceLocation()
    }
}
