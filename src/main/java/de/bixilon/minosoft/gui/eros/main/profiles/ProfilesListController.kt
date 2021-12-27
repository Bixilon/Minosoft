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

import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.TranslatableComponents
import de.bixilon.minosoft.data.text.events.ClickEvent
import de.bixilon.minosoft.gui.eros.controller.EmbeddedJavaFXController
import de.bixilon.minosoft.gui.eros.dialog.SimpleErosConfirmationDialog
import de.bixilon.minosoft.gui.eros.dialog.profiles.ProfileCreateDialog
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.ctext
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.delegate.watcher.entry.MapDelegateWatcher.Companion.watchMapFX
import javafx.fxml.FXML
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.layout.*


class ProfilesListController : EmbeddedJavaFXController<Pane>() {
    @FXML private lateinit var profilesListViewFX: ListView<Profile>
    @FXML private lateinit var profileInfoFX: AnchorPane

    @FXML private lateinit var createProfileButtonFX: Button

    var profileManager: ProfileManager<Profile>? = null
        set(value) {
            check(value != null)
            field = value
        }

    override fun init() {
        profilesListViewFX.setCellFactory {
            val controller = ProfileCardController.build()
            controller.profileList = this

            controller.root.setOnMouseClicked {
                if (it.clickCount != 2) {
                    return@setOnMouseClicked
                }
                profileManager?.selected = controller.item ?: return@setOnMouseClicked
            }
            return@setCellFactory controller
        }

        profilesListViewFX.selectionModel.selectedItemProperty().addListener { _, _, new ->
            setProfileInfo(new)
        }
        createProfileButtonFX.ctext = CREATE
    }

    override fun postInit() {
        root.setOnKeyPressed { profilesListViewFX.selectionModel.select(null) } // ToDo: Only on escape; not working
    }

    fun initWatch() {
        profileManager!!::profiles.watchMapFX(this) {
            profilesListViewFX.items -= it.valueRemoved
            profilesListViewFX.items += it.valueAdded
            profilesListViewFX.refresh()
        }
    }

    @FXML
    fun refreshList() {
        if (profileManager == null) {
            return
        }
        val selected = profilesListViewFX.selectionModel.selectedItem
        profilesListViewFX.items.clear()

        for (profile in profileManager!!.profiles.values) {
            updateProfile(profile)
        }

        profilesListViewFX.items.contains(selected).decide(selected, null).let {
            profilesListViewFX.selectionModel.select(it)

            profilesListViewFX.scrollTo(it)
        }
    }

    private fun updateProfile(profile: Profile) {
        val wasSelected = profilesListViewFX.selectionModel.selectedItem === profile
        // Platform.runLater {serverListViewFX.items.remove(card)}


        if (!profilesListViewFX.items.contains(profile)) {
            profilesListViewFX.items.add(profile)
        }


        if (wasSelected) {
            profilesListViewFX.selectionModel.select(profile)
        }
    }


    private fun setProfileInfo(profile: Profile?) {
        val profileManager = this.profileManager
        if (profile == null || profileManager == null) {
            profileInfoFX.children.clear()
            return
        }

        val pane = GridPane()

        AnchorPane.setLeftAnchor(pane, 10.0)
        AnchorPane.setRightAnchor(pane, 10.0)


        GridPane().let {
            var row = 0

            for ((key, property) in PROFILE_INFO_PROPERTIES) {
                val propertyValue = property(profile) ?: continue

                it.add(Minosoft.LANGUAGE_MANAGER.translate(key).textFlow, 0, row)
                it.add(ChatComponent.of(propertyValue).textFlow, 1, row++)
            }

            it.columnConstraints += ColumnConstraints(10.0, 180.0, 250.0)
            it.columnConstraints += ColumnConstraints(10.0, 200.0, 300.0)
            it.hgap = 10.0
            it.vgap = 5.0

            pane.add(it, 0, 0)
        }

        GridPane().let {
            it.columnConstraints += ColumnConstraints()
            it.columnConstraints += ColumnConstraints()
            it.columnConstraints += ColumnConstraints(0.0, -1.0, Double.POSITIVE_INFINITY, Priority.ALWAYS, HPos.LEFT, true)

            it.add(Button("Delete").apply {
                isDisable = !profile.manager.canDelete(profile)
                setOnAction {
                    SimpleErosConfirmationDialog(confirmButtonText = "minosoft:general.delete".toResourceLocation(), onConfirm = {
                        profile.manager.deleteAsync(profile)
                        JavaFXUtil.runLater {
                            profilesListViewFX.items.remove(profile)
                            setProfileInfo(profilesListViewFX.selectionModel.selectedItem)
                        }
                    }).show()
                }
                ctext = TranslatableComponents.GENERAL_DELETE
            }, 0, 0)
            it.add(Button("Edit").apply {
                // ToDo: Profile editing
                isDisable = true
                ctext = EDIT
            }, 1, 0)

            it.add(Button("Set primary").apply {
                isDisable = profile.manager.selected == profile
                setOnAction {
                    profile.manager.selected = profile
                    isDisable = true
                }
                ctext = SET_PRIMARY
            }, 3, 0)


            it.hgap = 5.0
            GridPane.setMargin(it, Insets(20.0, 0.0, 0.0, 0.0))

            pane.add(it, 0, 1)
        }


        profileInfoFX.children.setAll(pane)
    }


    @FXML
    fun createProfile() {
        val profileManager = profileManager ?: return
        ProfileCreateDialog(profileManager, false) { manager, profile ->
            if (manager !== profileManager) {
                return@ProfileCreateDialog
            }
            updateProfile(profile)
        }.show()
    }


    companion object {
        val LAYOUT = "minosoft:eros/main/profiles/profiles_list.fxml".toResourceLocation()

        private val EDIT = "minosoft:profiles.profile.list.button.edit".toResourceLocation()
        private val SET_PRIMARY = "minosoft:profiles.profile.list.button.set_primary".toResourceLocation()
        private val CREATE = "minosoft:profiles.profile.list.button.create".toResourceLocation()

        private val PROFILE_INFO_PROPERTIES: List<Pair<ResourceLocation, (Profile) -> Any?>> = listOf(
            "minosoft:profiles.profile.name".toResourceLocation() to { it.name },
            "minosoft:profiles.profile.description".toResourceLocation() to { it.description },

            "minosoft:general.empty".toResourceLocation() to { " " },

            "minosoft:profiles.profile.disk_path".toResourceLocation() to {
                val path = it.manager.getPath(it.name)
                TextComponent(it.manager.getPath(it.name), clickEvent = ClickEvent(ClickEvent.ClickEventActions.OPEN_FILE, path))
            },
        )
    }
}
