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

package de.bixilon.minosoft.gui.eros.main.profiles.type

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.gui.eros.card.AbstractCardController
import de.bixilon.minosoft.gui.eros.card.CardFactory
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.delegate.watcher.SimpleDelegateWatcher.Companion.watchFX
import javafx.fxml.FXML
import javafx.scene.text.TextFlow
import org.kordamp.ikonli.javafx.FontIcon

class ProfilesTypeCardController : AbstractCardController<ProfileManager<*>>() {
    @FXML private lateinit var iconFX: FontIcon
    @FXML private lateinit var headerFX: TextFlow
    @FXML private lateinit var textFX: TextFlow

    override fun updateItem(item: ProfileManager<*>?, empty: Boolean) {
        val previous = this.item
        super.updateItem(item, empty)
        item ?: return
        if (previous === item) {
            return
        }


        iconFX.isVisible = true

        iconFX.iconCode = item.icon
        headerFX.text = Minosoft.LANGUAGE_MANAGER.translate(item.namespace)

        recalculate(item)
        item::profiles.watchFX(this) { recalculate(item) } // ToDo: Not a watchable map yet
    }

    private fun recalculate(item: ProfileManager<*>) {
        textFX.text = "${item.profiles.size} profiles"
    }


    override fun clear() {
        iconFX.isVisible = false
        headerFX.children.clear()
        textFX.children.clear()
    }

    companion object : CardFactory<ProfilesTypeCardController> {
        override val LAYOUT = "minosoft:eros/main/profiles/profiles_type_card.fxml".toResourceLocation()
    }
}
