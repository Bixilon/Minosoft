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

import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.eros.controller.EmbeddedJavaFXController
import de.bixilon.minosoft.gui.eros.main.InfoPane
import de.bixilon.minosoft.modding.loader.ModLoader
import de.bixilon.minosoft.modding.loader.mod.MinosoftMod
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.layout.Pane

class ModsController : EmbeddedJavaFXController<Pane>() {
    @FXML private lateinit var modsListViewFX: ListView<MinosoftMod>
    @FXML private lateinit var modInfoFX: InfoPane<MinosoftMod>


    override fun init() {
        modsListViewFX.setCellFactory { ModCardController.build() }

        modsListViewFX.items += ModLoader.mods
        modsListViewFX.selectionModel.selectedItemProperty().addListener { _, _, new ->
            modInfoFX.update(new, MOD_INFO_PROPERTIES, arrayOf())
        }
    }


    companion object {
        val LAYOUT = "minosoft:eros/main/mods/mods.fxml".toResourceLocation()

        private val MOD_INFO_PROPERTIES: List<Pair<ResourceLocation, (MinosoftMod) -> Any?>> = listOf(
            "minosoft:mod.name".toResourceLocation() to { it.manifest?.name },
            "minosoft:mod.description".toResourceLocation() to { it.manifest?.description },
        )
    }
}
