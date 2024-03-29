/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.data.language.IntegratedLanguage
import de.bixilon.minosoft.gui.eros.card.AbstractCardController
import de.bixilon.minosoft.gui.eros.card.CardFactory
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.delegate.JavaFXDelegate.observeMapFX
import javafx.fxml.FXML
import javafx.scene.text.TextFlow
import org.kordamp.ikonli.javafx.FontIcon

class AccountTypeCardController : AbstractCardController<ErosAccountType<*>>() {
    @FXML private lateinit var iconFX: FontIcon
    @FXML private lateinit var headerFX: TextFlow
    @FXML private lateinit var textFX: TextFlow

    override fun updateItem(item: ErosAccountType<*>?, empty: Boolean) {
        val previous = this.item
        super.updateItem(item, empty)
        item ?: return
        if (previous === item) {
            return
        }


        iconFX.isVisible = true

        iconFX.iconCode = item.icon
        headerFX.text = IntegratedLanguage.LANGUAGE.translate(item)

        recalculate(item)
        ErosProfileManager.selected.general.accountProfile::entries.observeMapFX(this) { recalculate(item) }
    }

    private fun recalculate(type: ErosAccountType<*>) {
        var count = 0
        val profile = ErosProfileManager.selected.general.accountProfile
        for (account in profile.entries.values) {
            if (account.type != type.identifier) {
                continue
            }
            count++
        }
        textFX.text = "$count accounts" // ToDo: Update on the fly
    }


    override fun clear() {
        iconFX.isVisible = false
        headerFX.children.clear()
        textFX.children.clear()
    }

    companion object : CardFactory<AccountTypeCardController> {
        override val LAYOUT = "minosoft:eros/main/account/account_type_card.fxml".toResourceLocation()
    }
}
