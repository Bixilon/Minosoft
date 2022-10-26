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

package de.bixilon.minosoft.gui.eros.main.mods

import de.bixilon.minosoft.gui.eros.card.AbstractCardController
import de.bixilon.minosoft.gui.eros.card.CardFactory
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.modding.loader.mod.MinosoftMod
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.PixelImageView
import javafx.fxml.FXML
import javafx.scene.image.Image
import javafx.scene.text.TextFlow
import java.io.ByteArrayInputStream

class ModCardController : AbstractCardController<MinosoftMod>() {
    @FXML private lateinit var imageFX: PixelImageView
    @FXML private lateinit var nameFX: TextFlow
    @FXML private lateinit var descriptionFX: TextFlow

    override fun updateItem(item: MinosoftMod?, empty: Boolean) {
        val previous = this.item
        super.updateItem(item, empty)
        item ?: return

        if (previous === item) {
            return
        }


        imageFX.isVisible = true

        imageFX.image = item.assetsManager?.image?.let { Image(ByteArrayInputStream(it)) } ?: JavaFXUtil.MINOSOFT_LOGO
        nameFX.text = item.manifest?.name
        descriptionFX.text = item.manifest?.description ?: ""
    }

    override fun clear() {
        imageFX.isVisible = false
        nameFX.children.clear()
        descriptionFX.children.clear()
    }

    companion object : CardFactory<ModCardController> {
        override val LAYOUT = "minosoft:eros/main/mods/mod_type_card.fxml".toResourceLocation()
    }
}
