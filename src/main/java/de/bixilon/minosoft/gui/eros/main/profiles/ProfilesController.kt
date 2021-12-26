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

package de.bixilon.minosoft.gui.eros.main.profiles

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.gui.eros.controller.EmbeddedJavaFXController
import de.bixilon.minosoft.gui.eros.main.profiles.type.ProfilesTypeCardController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.layout.Pane

class ProfilesController : EmbeddedJavaFXController<Pane>() {
    @FXML private lateinit var managerContentFX: Pane
    @FXML private lateinit var managerTypeListViewFX: ListView<ProfileManager<*>>


    private lateinit var currentController: EmbeddedJavaFXController<*>

    override fun init() {
        managerTypeListViewFX.setCellFactory { ProfilesTypeCardController.build() }
        for (manager in GlobalProfileManager.DEFAULT_MANAGERS.values) {
            managerTypeListViewFX.items += manager
        }
        // ToDo
        check(managerTypeListViewFX.items.size > 0)


        managerTypeListViewFX.selectionModel.selectedItemProperty().addListener { _, _, new ->
            new ?: return@addListener // Should not happen
            if (this::currentController.isInitialized) {
                val currentController = this.currentController
                if (currentController is ProfilesListController) {
                    currentController.profileManager = new.unsafeCast()
                    currentController.initWatch()
                    currentController.refreshList()
                }
                return@addListener
            }
            currentController = JavaFXUtil.loadEmbeddedController<ProfilesListController>(ProfilesListController.LAYOUT).apply {
                profileManager = new.unsafeCast()
                initWatch()
                refreshList()
                managerContentFX.children.setAll(this.root)
            }
        }

        managerTypeListViewFX.selectionModel.select(0)
    }

    companion object {
        val LAYOUT = "minosoft:eros/main/profiles/profiles.fxml".toResourceLocation()
    }
}
