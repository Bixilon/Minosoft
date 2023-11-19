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

package de.bixilon.minosoft.gui.eros.dialog.profiles

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.profile.manager.ProfileManagers
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.storage.ProfileIOUtil.isValidName
import de.bixilon.minosoft.config.profile.storage.StorageProfileManager
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.eros.controller.JavaFXWindowController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.ctext
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.placeholder
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.text.TextFlow
import javafx.stage.Modality

class ProfileCreateDialog<T : Profile>(
    private val manager: StorageProfileManager<T>,
    private val strictType: Boolean,
    private val onCreate: (manager: StorageProfileManager<*>, profile: Profile) -> Unit,
) : JavaFXWindowController() {
    @FXML private lateinit var headerFX: TextFlow
    @FXML private lateinit var descriptionFX: TextFlow

    @FXML private lateinit var typeLabelFX: TextFlow
    @FXML private lateinit var typeFX: ComboBox<ResourceLocation>
    @FXML private lateinit var nameLabelFX: TextFlow
    @FXML private lateinit var nameFX: TextField

    @FXML private lateinit var createButtonFX: Button
    @FXML private lateinit var cancelButtonFX: Button


    public override fun show() {
        JavaFXUtil.runLater {
            JavaFXUtil.openModal(TITLE, LAYOUT, this, modality = Modality.APPLICATION_MODAL)
            super.show()
        }
    }

    override fun init() {
        super.init()

        if (strictType) {
            typeFX.items += manager.type.identifier
            typeFX.selectionModel.select(manager.type.identifier)
            typeFX.isDisable = true
        } else {
            for (namespace in ProfileManagers) {
                typeFX.items += namespace.identifier
            }
            typeFX.selectionModel.select(manager.type.identifier)
        }


        headerFX.text = HEADER
        descriptionFX.text = DESCRIPTION
        typeLabelFX.text = TYPE_LABEL
        nameLabelFX.text = NAME_LABEL
        nameFX.placeholder = NAME_PLACEHOLDER
        createButtonFX.ctext = CREATE_BUTTON
        cancelButtonFX.ctext = CANCEL_BUTTON

        nameFX.textProperty().addListener { _, _, new ->
            createButtonFX.isDisable = !new.isValidName() || manager[new] != null
        }
    }

    override fun postInit() {
        super.postInit()

        stage.scene.root.addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.ESCAPE) {
                cancel()
            }
        }
    }

    @FXML
    fun create() {
        if (createButtonFX.isDisable) return

        val manager: StorageProfileManager<T> = if (strictType) manager else ProfileManagers[typeFX.selectionModel.selectedItem]?.unsafeCast() ?: return
        val profile = manager.create(nameFX.text)
        onCreate(manager, profile)
        close()
    }

    @FXML
    fun cancel() {
        close()
    }

    companion object {
        private val LAYOUT = "minosoft:eros/dialog/profiles/create.fxml".toResourceLocation()

        private val TITLE = "minosoft:general.dialog.profile.create.title".toResourceLocation()
        private val HEADER = "minosoft:general.dialog.profile.create.header".toResourceLocation()
        private val DESCRIPTION = "minosoft:general.dialog.profile.create.description".toResourceLocation()
        private val TYPE_LABEL = "minosoft:general.dialog.profile.create.type.label".toResourceLocation()
        private val NAME_LABEL = "minosoft:general.dialog.profile.create.name.label".toResourceLocation()
        private val NAME_PLACEHOLDER = "minosoft:general.dialog.profile.create.name.placeholder".toResourceLocation()
        private val CREATE_BUTTON = "minosoft:general.dialog.profile.create.create_button".toResourceLocation()
        private val CANCEL_BUTTON = "minosoft:general.dialog.profile.create.cancel_button".toResourceLocation()
    }
}
