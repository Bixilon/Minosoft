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

package de.bixilon.minosoft.gui.eros.dialog

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.gui.eros.controller.DialogController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.text.TextFlow
import javafx.stage.Modality

class ProfileSelectDialog(
    profiles: MutableMap<ResourceLocation, String>,
    private val onConfirm: (Map<ResourceLocation, String>) -> Unit,
    private val onCancel: () -> Unit,
) : DialogController() {
    private val profiles = profiles.toMutableMap()
    @FXML private lateinit var headerFX: TextFlow

    @FXML private lateinit var profilesFX: TableView<ProfileEntry>
    @FXML private lateinit var typeColumnFX: TableColumn<ProfileEntry, ResourceLocation>
    @FXML private lateinit var valueColumnFX: TableColumn<ProfileEntry, String>

    @FXML private lateinit var createProfileButtonFX: Button
    @FXML private lateinit var cancelButtonFX: Button
    @FXML private lateinit var confirmButtonFX: Button

    fun show() {
        JavaFXUtil.runLater {
            JavaFXUtil.openModal(TITLE, LAYOUT, this, Modality.APPLICATION_MODAL)
            stage.show()
        }
    }

    override fun init() {
        headerFX.text = Minosoft.LANGUAGE_MANAGER.translate(HEADER)

        typeColumnFX.setCellFactory { ResourceLocationCell() }
        valueColumnFX.setCellFactory { ProfileCell() }

        for ((type, profile) in profiles) {
            profilesFX.items += ProfileEntry(type, profile)
        }
        profilesFX.items += ProfileEntry(null, "")
    }

    override fun postInit() {
        super.postInit()

        stage.setOnCloseRequest { cancel() }

        stage.scene.root.addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.ENTER) {
                cancel()
            }
        }
    }

    @FXML
    fun newProfile() {
    }

    @FXML
    fun confirm() {
        stage.close()
        val profiles: MutableMap<ResourceLocation, String> = mutableMapOf()
        for ((resourceLocation, profile) in this.profilesFX.items) {
            if (profile.isBlank()) {
                continue
            }
            profiles[resourceLocation ?: continue] = profile
        }
        onConfirm(profiles)
    }

    @FXML
    fun cancel() {
        onCancel()
        stage.close()
    }


    companion object {
        private val LAYOUT = "minosoft:eros/dialog/profile_select.fxml".toResourceLocation()
        private val TITLE = "minosoft:general.dialog.profile_select.title".toResourceLocation()
        private val HEADER = "minosoft:general.dialog.profile_select.header".toResourceLocation()
    }

    private data class ProfileEntry(
        var resourceLocation: ResourceLocation?,
        var profile: String = "",
    )

    private abstract inner class EditTableCell<T> : TableCell<ProfileEntry, T>() {
        protected val label = Label()
        protected val comboBox = ComboBox<T>()

        init {
            graphic = label
            selectedProperty().addListener { _, _, _ -> cancelEdit() }
            comboBox.selectionModel.selectedItemProperty().addListener { _, _, selected ->
                commitEdit(selected)
            }
        }

        override fun updateItem(item: T, empty: Boolean) {
            super.updateItem(item, empty)
            graphic = label
            isVisible = !empty
            if (empty) {
                return
            }
            update(tableRow.item)
        }

        abstract fun update(entry: ProfileEntry)
        abstract fun updateItem(item: T)

        override fun startEdit() {
            super.startEdit()
            graphic = comboBox
            comboBox.show()
        }

        override fun cancelEdit() {
            super.cancelEdit()
            graphic = label
        }

        override fun commitEdit(newValue: T?) {
            super.commitEdit(newValue)
            updateItem(newValue ?: return)
            graphic = label
        }
    }

    private inner class ResourceLocationCell : EditTableCell<ResourceLocation?>() {
        init {
            comboBox.selectionModel.selectedItemProperty().addListener { _, previous, selected ->
                if (previous == null && selected != null) {
                    tableView.items += ProfileEntry(null, "")
                    tableView.refresh()
                }
            }
        }

        override fun startEdit() {
            val thisNamespace = tableRow.item.resourceLocation
            if (comboBox.items.isEmpty()) {
                val alreadyDisplayed: MutableSet<ResourceLocation?> = mutableSetOf()
                for (entry in tableView.items) {
                    alreadyDisplayed += entry.resourceLocation ?: continue
                }
                for ((namespace, profileManager) in GlobalProfileManager.DEFAULT_MANAGERS) {
                    if (!profileManager.profileSelectable || (thisNamespace != namespace && namespace in alreadyDisplayed)) {
                        continue
                    }
                    comboBox.items += namespace
                }
            }
            comboBox.selectionModel.select(thisNamespace)
            super.startEdit()
        }


        override fun update(entry: ProfileEntry) = updateItem(entry.resourceLocation)


        override fun updateItem(item: ResourceLocation?) {
            label.text = item?.toString() ?: "Double click to select"
            tableRow.item.resourceLocation = item
        }
    }

    private inner class ProfileCell : EditTableCell<String>() {

        override fun startEdit() {
            comboBox.items.clear()
            comboBox.setCellFactory {
                object : ListCell<String>() {
                    override fun updateItem(item: String?, empty: Boolean) {
                        super.updateItem(item, empty)
                        text = if (item?.isBlank() == true && !empty) {
                            "<None>"
                        } else {
                            item.toString()
                        }
                    }
                }
            }
            comboBox.items += ""
            GlobalProfileManager[tableRow.item.resourceLocation ?: return]?.let {
                comboBox.items += it.profiles.keys
            }
            comboBox.selectionModel.select(this.tableRow.item.profile)
            super.startEdit()
        }

        override fun update(entry: ProfileEntry) = updateItem(entry.profile)

        override fun updateItem(item: String) {
            if (item.isBlank()) {
                label.text = "<None>"
            } else {
                label.text = item
            }
            tableRow.item.profile = item
        }
    }
}
