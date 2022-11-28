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

package de.bixilon.minosoft.gui.eros.main.profiles

import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.eros.card.AbstractCardController
import de.bixilon.minosoft.gui.eros.card.CardFactory
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.delegate.JavaFXDelegate.observeFX
import javafx.fxml.FXML
import javafx.scene.text.TextFlow

class ProfileCardController : AbstractCardController<Profile>() {
    @FXML private lateinit var profileNameFX: TextFlow
    @FXML private lateinit var profileDescriptionFX: TextFlow

    lateinit var profileList: ProfilesListController


    override fun clear() {
        profileNameFX.children.clear()
        profileDescriptionFX.children.clear()
    }

    override fun updateItem(item: Profile?, empty: Boolean) {
        val previous = this.item
        super.updateItem(item, empty)
        root.isVisible = !empty
        item ?: return
        if (previous === item) {
            return
        }


        profileNameFX.text = item.name
        item::description.observeFX(this, true) { profileDescriptionFX.text = it }
    }

    companion object : CardFactory<ProfileCardController> {
        override val LAYOUT: ResourceLocation = "minosoft:eros/main/profiles/profile_card.fxml".toResourceLocation()
    }
}
