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

package de.bixilon.minosoft.gui.eros.main.profiles.type

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.map.bi.AbstractMutableBiMap
import de.bixilon.kutil.observer.map.MapChange
import de.bixilon.kutil.observer.map.bi.BiMapObserver.Companion.observeBiMap
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.gui.eros.card.AbstractCardController
import de.bixilon.minosoft.gui.eros.card.CardFactory
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.fxml.FXML
import javafx.scene.text.TextFlow
import org.kordamp.ikonli.javafx.FontIcon
import kotlin.reflect.KProperty0

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
        headerFX.text = Minosoft.LANGUAGE_MANAGER.forceTranslate(item.namespace)

        recalculate(item)
        item::profiles.observeBiMapFX(this) { recalculate(item) }
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


        private fun <K> KProperty0<AbstractMutableBiMap<K, *>>.observeBiMapFX(owner: Any, observer: (MapChange<K, Profile>) -> Unit) {
            this.unsafeCast<KProperty0<AbstractMutableBiMap<K, Profile>>>().observeBiMap(owner) { JavaFXUtil.runLater { observer(it) } }
        }
    }
}
