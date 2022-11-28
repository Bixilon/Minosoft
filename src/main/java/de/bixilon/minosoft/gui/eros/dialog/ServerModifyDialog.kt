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

package de.bixilon.minosoft.gui.eros.dialog

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.ErosServer
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.data.registries.versions.VersionTypes
import de.bixilon.minosoft.data.registries.versions.Versions
import de.bixilon.minosoft.data.text.TranslatableComponents
import de.bixilon.minosoft.gui.eros.controller.DialogController
import de.bixilon.minosoft.gui.eros.dialog.profiles.ProfileSelectDialog
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.ctext
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.placeholder
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.DNSUtil
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.text.TextFlow

/**
 * Used to add or edit a server
 */
class ServerModifyDialog(
    private val server: ErosServer? = null,
    val onUpdate: (name: String, address: String, forcedVersion: Version?, profiles: Map<ResourceLocation, String>, queryVersion: Boolean) -> Unit,
) : DialogController() {
    @FXML private lateinit var descriptionFX: TextFlow
    @FXML private lateinit var serverNameLabelFX: TextFlow
    @FXML private lateinit var serverNameFX: TextField
    @FXML private lateinit var serverAddressLabelFX: TextFlow
    @FXML private lateinit var serverAddressFX: TextField

    @FXML private lateinit var forcedVersionLabelFX: TextFlow
    @FXML private lateinit var forcedVersionFX: ComboBox<Version>
    @FXML private lateinit var showReleasesFX: CheckBox
    @FXML private lateinit var showSnapshotsFX: CheckBox

    @FXML private lateinit var profilesLabelFX: TextFlow
    @FXML private lateinit var openProfileSelectDialogButtonFX: Button

    @FXML private lateinit var modifyServerButtonFX: Button
    @FXML private lateinit var cancelButtonFX: Button

    @FXML private lateinit var optionQueryVersionFX: CheckBox


    private var profileSelectDialog: ProfileSelectDialog? = null

    private var profiles = server?.profiles ?: mutableMapOf()

    public override fun show() {
        JavaFXUtil.runLater {
            JavaFXUtil.openModal((server == null).decide(ADD_TITLE, EDIT_TITLE), LAYOUT, this)
            super.show()
        }
    }

    private fun refreshVersions() {
        val selected = forcedVersionFX.selectionModel.selectedItem
        forcedVersionFX.items.clear()
        for (version in Versions) {
            if (version.type == VersionTypes.RELEASE && !showReleasesFX.isSelected) {
                continue
            }
            if ((version.type == VersionTypes.SNAPSHOT || version.type == VersionTypes.APRIL_FOOL) && !showSnapshotsFX.isSelected) {
                continue
            }
            forcedVersionFX.items += version
        }

        forcedVersionFX.items += Versions.AUTOMATIC

        forcedVersionFX.items.sortByDescending { it.sortingId }

        if (forcedVersionFX.items.contains(selected)) {
            forcedVersionFX.selectionModel.select(selected)
        } else {
            forcedVersionFX.selectionModel.select(Versions.AUTOMATIC)
        }
    }

    override fun init() {
        serverNameLabelFX.text = SERVER_NAME_LABEL
        serverNameFX.placeholder = SERVER_NAME_PLACEHOLDER
        serverAddressLabelFX.text = SERVER_ADDRESS_LABEL
        serverAddressFX.placeholder = SERVER_ADDRESS_PLACEHOLDER
        forcedVersionLabelFX.text = FORCED_VERSION_LABEL
        profilesLabelFX.text = PROFILES_LABEL
        openProfileSelectDialogButtonFX.ctext = PROFILES_OPEN_PROFILE_SELECT
        optionQueryVersionFX.ctext = OPTION_QUERY_VERSION

        cancelButtonFX.ctext = TranslatableComponents.GENERAL_CANCEL

        val modifyConfig = ErosProfileManager.selected.server.modify

        modifyConfig::showReleases.observe(this) { showReleasesFX.isSelected = it }
        modifyConfig::showSnapshots.observe(this) { showSnapshotsFX.isSelected = it }

        showReleasesFX.apply {
            isSelected = modifyConfig.showReleases
            ctext = SHOW_RELEASES
            setOnAction {
                modifyConfig.showReleases = isSelected
                refreshVersions()
            }
        }
        showSnapshotsFX.apply {
            isSelected = modifyConfig.showSnapshots
            ctext = SHOW_SNAPSHOTS
            setOnAction {
                modifyConfig.showSnapshots = isSelected
                refreshVersions()
            }
        }

        forcedVersionFX.setCellFactory {
            object : ListCell<Version>() {
                override fun updateItem(version: Version?, empty: Boolean) {
                    super.updateItem(version, empty)
                    version ?: return

                    text = if (version == Versions.AUTOMATIC) {
                        Minosoft.LANGUAGE_MANAGER.translate(VERSION_AUTOMATIC).message
                    } else {
                        "${version.name} (${version.type.name.lowercase()})"
                    }
                }
            }
        }
        forcedVersionFX.selectionModel.selectedItemProperty().addListener { _, _, next ->
            if (next == Versions.AUTOMATIC) {
                optionQueryVersionFX.isSelected = true
                optionQueryVersionFX.isDisable = true
            } else {
                optionQueryVersionFX.isDisable = false
            }
        }
        refreshVersions()

        if (server == null) {
            forcedVersionFX.selectionModel.select(Versions.AUTOMATIC)
            // add
            descriptionFX.text = ADD_DESCRIPTION
            modifyServerButtonFX.ctext = ADD_UPDATE_BUTTON
            optionQueryVersionFX.isSelected = true
        } else {
            forcedVersionFX.selectionModel.select(server.forcedVersion ?: Versions.AUTOMATIC)
            descriptionFX.text = EDIT_DESCRIPTION
            modifyServerButtonFX.ctext = EDIT_UPDATE_BUTTON

            serverNameFX.text = server.name.legacyText.removeSuffix("§r")
            serverAddressFX.text = server.address

            modifyServerButtonFX.isDisable = serverAddressFX.text.isBlank()
            optionQueryVersionFX.isSelected = server.queryVersion || server.forcedVersion == null
        }

        serverAddressFX.textProperty().addListener { _, _, new ->
            serverAddressFX.text = DNSUtil.fixAddress(new)

            modifyServerButtonFX.isDisable = serverAddressFX.text.isBlank()
        }
    }

    @FXML
    fun modify() {
        if (modifyServerButtonFX.isDisable) {
            return
        }
        val forcedVersion = (forcedVersionFX.selectionModel.selectedItem == Versions.AUTOMATIC).decide(null) { forcedVersionFX.selectionModel.selectedItem }
        DefaultThreadPool += { onUpdate(serverNameFX.text.isBlank().decide({ serverAddressFX.text.toString() }, { serverNameFX.text.trim() }), serverAddressFX.text, forcedVersion, profiles, optionQueryVersionFX.isSelected) }
        stage.close()
    }

    @FXML
    fun cancel() {
        stage.close()
    }

    @FXML
    fun openProfileSelectDialog() {
        profileSelectDialog?.let { it.show(); return }
        val dialog = ProfileSelectDialog(
            profiles = profiles,
            onConfirm = {
                this.profiles = it.toMutableMap()
                this.profileSelectDialog = null
            },
            onCancel = {
                this.profileSelectDialog = null
            },
        )
        this.profileSelectDialog = dialog
        dialog.show()
    }


    companion object {
        private val LAYOUT = "minosoft:eros/dialog/modify_server.fxml".toResourceLocation()

        private val SERVER_NAME_LABEL = "minosoft:modify_server.name.label".toResourceLocation()
        private val SERVER_NAME_PLACEHOLDER = "minosoft:modify_server.name.placeholder".toResourceLocation()
        private val SERVER_ADDRESS_LABEL = "minosoft:modify_server.address.label".toResourceLocation()
        private val SERVER_ADDRESS_PLACEHOLDER = "minosoft:modify_server.address.placeholder".toResourceLocation()
        private val FORCED_VERSION_LABEL = "minosoft:modify_server.forced_version.label".toResourceLocation()
        private val VERSION_AUTOMATIC = "minosoft:modify_server.forced_version.automatic".toResourceLocation()
        private val SHOW_RELEASES = "minosoft:modify_server.forced_version.releases".toResourceLocation()
        private val SHOW_SNAPSHOTS = "minosoft:modify_server.forced_version.snapshots".toResourceLocation()
        private val PROFILES_LABEL = "minosoft:modify_server.profiles.label".toResourceLocation()
        private val PROFILES_OPEN_PROFILE_SELECT = "minosoft:modify_server.profiles.open_select_dialog".toResourceLocation()
        private val OPTION_QUERY_VERSION = "minosoft:modify_server.profiles.option_query_version".toResourceLocation()

        private val ADD_TITLE = "minosoft:modify_server.add.title".toResourceLocation()
        private val ADD_DESCRIPTION = "minosoft:modify_server.add.description".toResourceLocation()
        private val ADD_UPDATE_BUTTON = "minosoft:modify_server.add.update_button".toResourceLocation()


        private val EDIT_TITLE = "minosoft:modify_server.edit.title".toResourceLocation()
        private val EDIT_DESCRIPTION = "minosoft:modify_server.edit.description".toResourceLocation()
        private val EDIT_UPDATE_BUTTON = "minosoft:modify_server.edit.update_button".toResourceLocation()
    }
}
