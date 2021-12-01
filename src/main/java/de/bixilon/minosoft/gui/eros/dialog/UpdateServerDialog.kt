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
import de.bixilon.minosoft.config.profile.change.listener.SimpleProfileChangeListener.Companion.listenFX
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.config.server.Server
import de.bixilon.minosoft.data.registries.versions.Version
import de.bixilon.minosoft.data.registries.versions.VersionTypes
import de.bixilon.minosoft.data.registries.versions.Versions
import de.bixilon.minosoft.data.text.TranslatableComponents
import de.bixilon.minosoft.gui.eros.controller.DialogController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.ctext
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.placeholder
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.DNSUtil
import de.bixilon.minosoft.util.KUtil.decide
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.task.pool.DefaultThreadPool
import javafx.fxml.FXML
import javafx.scene.control.*
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.text.TextFlow

/**
 * Used to add or edit a server
 */
class UpdateServerDialog(
    private val server: Server? = null,
    val onUpdate: (name: String, address: String, forcedVersion: Version?) -> Unit,
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


    @FXML private lateinit var updateServerButtonFX: Button

    @FXML private lateinit var cancelButtonFX: Button


    fun show() {
        JavaFXUtil.runLater {
            JavaFXUtil.openModal((server == null).decide(ADD_TITLE, EDIT_TITLE), LAYOUT, this)
            stage.show()
        }
    }

    private fun refreshVersions() {
        val selected = forcedVersionFX.selectionModel.selectedItem
        forcedVersionFX.items.clear()
        for (version in Versions.VERSION_ID_MAP.values) {
            if (version.type == VersionTypes.RELEASE && !showReleasesFX.isSelected) {
                continue
            }
            if (version.type == VersionTypes.SNAPSHOT && !showSnapshotsFX.isSelected) {
                continue
            }
            forcedVersionFX.items += version
        }

        forcedVersionFX.items += Versions.AUTOMATIC_VERSION

        forcedVersionFX.items.sortByDescending { it.sortingId }

        if (forcedVersionFX.items.contains(selected)) {
            forcedVersionFX.selectionModel.select(selected)
        } else {
            forcedVersionFX.selectionModel.select(Versions.AUTOMATIC_VERSION)
        }
    }

    override fun init() {
        serverNameLabelFX.text = SERVER_NAME_LABEL
        serverNameFX.placeholder = SERVER_NAME_PLACEHOLDER
        serverAddressLabelFX.text = SERVER_ADDRESS_LABEL
        serverAddressFX.placeholder = SERVER_ADDRESS_PLACEHOLDER
        forcedVersionLabelFX.text = FORCED_VERSION_LABEL

        cancelButtonFX.ctext = TranslatableComponents.GENERAL_CANCEL

        val modifyConfig = ErosProfileManager.selected.server.modify

        modifyConfig::showReleases.listenFX(this) { showReleasesFX.isSelected = it }
        modifyConfig::showSnapshots.listenFX(this) { showSnapshotsFX.isSelected = it }

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

                    text = if (version == Versions.AUTOMATIC_VERSION) {
                        Minosoft.LANGUAGE_MANAGER.translate(VERSION_AUTOMATIC).message
                    } else {
                        "${version.name} (${version.type.name.lowercase()})"
                    }
                }
            }
        }
        refreshVersions()

        if (server == null) {
            forcedVersionFX.selectionModel.select(Versions.AUTOMATIC_VERSION)
            // add
            descriptionFX.text = ADD_DESCRIPTION
            updateServerButtonFX.ctext = ADD_UPDATE_BUTTON
        } else {
            forcedVersionFX.selectionModel.select(server.forcedVersion ?: Versions.AUTOMATIC_VERSION)
            descriptionFX.text = EDIT_DESCRIPTION
            updateServerButtonFX.ctext = EDIT_UPDATE_BUTTON

            serverNameFX.text = server.name.legacyText.removeSuffix("§r")
            serverAddressFX.text = server.address

            updateServerButtonFX.isDisable = serverAddressFX.text.isBlank()
        }

        serverAddressFX.textProperty().addListener { _, _, new ->
            serverAddressFX.text = DNSUtil.fixAddress(new)

            updateServerButtonFX.isDisable = serverAddressFX.text.isBlank()
        }
    }

    override fun postInit() {
        super.postInit()


        stage.scene.root.addEventFilter(KeyEvent.KEY_PRESSED) {
            if (it.code == KeyCode.ENTER) {
                update()
            }
        }
    }

    @FXML
    fun update() {
        if (updateServerButtonFX.isDisable) {
            return
        }
        val forcedVersion = (forcedVersionFX.selectionModel.selectedItem == Versions.AUTOMATIC_VERSION).decide(null) { forcedVersionFX.selectionModel.selectedItem }
        DefaultThreadPool += { onUpdate(serverNameFX.text.isBlank().decide({ serverAddressFX.text.toString() }, { serverNameFX.text.trim() }), serverAddressFX.text, forcedVersion) }
        stage.close()
    }

    @FXML
    fun cancel() {
        stage.close()
    }


    companion object {
        private val LAYOUT = "minosoft:eros/dialog/update_server.fxml".toResourceLocation()

        private val SERVER_NAME_LABEL = "minosoft:update_server.name.label".toResourceLocation()
        private val SERVER_NAME_PLACEHOLDER = "minosoft:update_server.name.placeholder".toResourceLocation()
        private val SERVER_ADDRESS_LABEL = "minosoft:update_server.address.label".toResourceLocation()
        private val SERVER_ADDRESS_PLACEHOLDER = "minosoft:update_server.address.placeholder".toResourceLocation()
        private val FORCED_VERSION_LABEL = "minosoft:update_server.forced_version.label".toResourceLocation()
        private val VERSION_AUTOMATIC = "minosoft:update_server.forced_version.automatic".toResourceLocation()
        private val SHOW_RELEASES = "minosoft:update_server.forced_version.releases".toResourceLocation()
        private val SHOW_SNAPSHOTS = "minosoft:update_server.forced_version.snapshots".toResourceLocation()

        private val ADD_TITLE = "minosoft:update_server.add.title".toResourceLocation()
        private val ADD_DESCRIPTION = "minosoft:update_server.add.description".toResourceLocation()
        private val ADD_UPDATE_BUTTON = "minosoft:update_server.add.update_button".toResourceLocation()


        private val EDIT_TITLE = "minosoft:update_server.edit.title".toResourceLocation()
        private val EDIT_DESCRIPTION = "minosoft:update_server.edit.description".toResourceLocation()
        private val EDIT_UPDATE_BUTTON = "minosoft:update_server.edit.update_button".toResourceLocation()
    }
}
