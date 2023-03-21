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
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.data.language.translate.Translatable
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.gui.eros.controller.DialogController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.ctext
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.fxml.FXML
import javafx.scene.control.*
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
    @FXML private lateinit var profileColumnFX: TableColumn<ProfileEntry, Any?>

    @FXML private lateinit var cancelButtonFX: Button
    @FXML private lateinit var confirmButtonFX: Button

    public override fun show() {
        JavaFXUtil.runLater {
            JavaFXUtil.openModal(TITLE, LAYOUT, this, Modality.APPLICATION_MODAL)
            super.show()
        }
    }

    override fun init() {
        headerFX.text = Minosoft.LANGUAGE_MANAGER.forceTranslate(HEADER)

        typeColumnFX.ctext = TYPE_COLUMN_TITLE
        profileColumnFX.ctext = PROFILE_COLUMN_TITLE

        typeColumnFX.setCellFactory { ResourceLocationCell() }
        profileColumnFX.setCellFactory { ProfileCell() }

        cancelButtonFX.ctext = CANCEL
        confirmButtonFX.ctext = CONFIRM

        for ((type, profile) in profiles) {
            profilesFX.items += ProfileEntry(type, profile)
        }
        profilesFX.items += ProfileEntry(null, "")
    }

    override fun postInit() {
        super.postInit()

        stage.setOnCloseRequest { cancel() }
    }

    @FXML
    fun confirm() {
        stage.close()
        val profiles: MutableMap<ResourceLocation, String> = mutableMapOf()
        for ((resourceLocation, profile) in this.profilesFX.items) {
            if (profile !is String) {
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
        val LAYOUT = "minosoft:eros/dialog/profiles/select.fxml".toResourceLocation()
        private val TITLE = "minosoft:general.dialog.profile.select.title".toResourceLocation()
        private val HEADER = "minosoft:general.dialog.profile.select.header".toResourceLocation()

        private val TYPE_COLUMN_TITLE = "minosoft:general.dialog.profile.select.column.type".toResourceLocation()
        private val PROFILE_COLUMN_TITLE = "minosoft:general.dialog.profile.select.column.profile".toResourceLocation()

        private val CANCEL = "minosoft:general.dialog.profile.select.cancel_button".toResourceLocation()
        private val CONFIRM = "minosoft:general.dialog.profile.select.confirm_button".toResourceLocation()

        private val CLICK_ME_TO_ADD = "minosoft:general.dialog.profile.select.click_me_to_add".toResourceLocation()
    }

    private data class ProfileEntry(
        var resourceLocation: ResourceLocation?,
        var profile: String?,
    )

    private abstract inner class EditTableCell<T> : TableCell<ProfileEntry, T>() {
        protected val label = Label()
        protected val comboBox = ComboBox<T>()

        init {
            graphic = label

            comboBox.maxWidth = Double.MAX_VALUE
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
                if (previous == null && selected != null && this.index == tableView.items.size - 1) {
                    tableView.items += ProfileEntry(null, "")
                    tableView.refresh()
                }
            }
            styleClass.add("table-row-cell")
        }

        override fun startEdit() {
            val thisNamespace: ResourceLocation? = tableRow.item.resourceLocation
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
            label.ctext = item?.toString() ?: CLICK_ME_TO_ADD
            tableRow.item.resourceLocation = item
        }
    }

    private inner class ProfileCell : EditTableCell<Any?>() {

        init {
            // forbid selection of "CREATE"
            comboBox.selectionModel.selectedItemProperty().addListener { _, _, next ->
                if (next == SelectSpecialOptions.CREATE) {
                    val profileManager: ProfileManager<Profile> = (GlobalProfileManager[this.tableRow.item.resourceLocation] ?: return@addListener).unsafeCast()
                    ProfileCreateDialog(profileManager, true) { _, profile ->
                        val name = profile.name
                        comboBox.items.add(name)
                        comboBox.selectionModel.select(name)
                    }.show()
                    comboBox.selectionModel.select(SelectSpecialOptions.NONE)
                }
            }
            styleClass.add("table-row-cell")
        }

        override fun startEdit() {
            comboBox.items.clear()
            comboBox.setCellFactory {
                object : ListCell<Any?>() {
                    override fun updateItem(item: Any?, empty: Boolean) {
                        super.updateItem(item, empty)
                        if (empty) {
                            return
                        }
                        ctext = item
                    }
                }
            }
            comboBox.items += SelectSpecialOptions.NONE
            comboBox.items += SelectSpecialOptions.CREATE

            GlobalProfileManager[tableRow.item.resourceLocation ?: return]?.let {
                for (profile in it.profiles.keys) {
                    comboBox.items += profile
                }
            }
            comboBox.selectionModel.select(this.tableRow.item.profile ?: SelectSpecialOptions.NONE)
            super.startEdit()
        }

        override fun update(entry: ProfileEntry) = updateItem(entry.profile)

        override fun updateItem(item: Any?) {
            label.ctext = item
            if (item is String) {
                tableRow.item.profile = item
            } else {
                tableRow.item.profile = null
            }
        }
    }


    private enum class SelectSpecialOptions(override val translationKey: ResourceLocation?) : Translatable {
        NONE("minosoft:general.dialog.profile.select.none".toResourceLocation()),
        CREATE("minosoft:general.dialog.profile.select.create".toResourceLocation()),
        ;
    }
}
