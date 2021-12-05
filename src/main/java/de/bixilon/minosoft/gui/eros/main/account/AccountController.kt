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

package de.bixilon.minosoft.gui.eros.main.account

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.profile.delegate.watcher.entry.MapProfileDelegateWatcher.Companion.profileWatchMapFX
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.accounts.types.MicrosoftAccount
import de.bixilon.minosoft.data.accounts.types.MojangAccount
import de.bixilon.minosoft.data.accounts.types.OfflineAccount
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.gui.eros.controller.EmbeddedJavaFXController
import de.bixilon.minosoft.gui.eros.dialog.SimpleErosConfirmationDialog
import de.bixilon.minosoft.gui.eros.main.account.add.MicrosoftAddController
import de.bixilon.minosoft.gui.eros.main.account.add.MojangAddController
import de.bixilon.minosoft.gui.eros.main.account.add.OfflineAddController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.util.KUtil.decide
import de.bixilon.minosoft.util.KUtil.extend
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.task.pool.DefaultThreadPool
import javafx.fxml.FXML
import javafx.geometry.HPos
import javafx.geometry.Insets
import javafx.scene.control.Button
import javafx.scene.control.ListView
import javafx.scene.layout.*
import org.kordamp.ikonli.fontawesome5.FontAwesomeBrands
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid

class AccountController : EmbeddedJavaFXController<Pane>() {
    @FXML private lateinit var accountTypeListViewFX: ListView<ErosAccountType<*>>

    @FXML private lateinit var accountListViewFX: ListView<Account>
    @FXML private lateinit var accountInfoFX: AnchorPane

    @FXML private lateinit var addButtonFX: Button


    override fun init() {
        accountTypeListViewFX.setCellFactory { AccountTypeCardController.build() }
        accountTypeListViewFX.items += ACCOUNT_TYPES

        accountListViewFX.setCellFactory { AccountCardController.build() }


        accountTypeListViewFX.selectionModel.selectedItemProperty().addListener { _, _, new ->
            refreshList()
            addButtonFX.isDisable = new.addHandler == null
        }


        accountListViewFX.selectionModel.selectedItemProperty().addListener { _, _, new ->
            setAccountInfo(new)
        }

        accountTypeListViewFX.selectionModel.select(0)

        addButtonFX.setOnAction {
            accountTypeListViewFX.selectionModel.selectedItem.addHandler?.invoke(this)
        }
    }

    fun refreshList() {
        val type = accountTypeListViewFX.selectionModel.selectedItem
        val selected = accountListViewFX.selectionModel.selectedItem
        accountListViewFX.items.clear()
        val profile = ErosProfileManager.selected.general.accountProfile
        for (account in profile.entries.values) {
            if (account.type != type.resourceLocation) {
                continue
            }
            accountListViewFX.items += account
        }
        profile::entries.profileWatchMapFX(this, profile) {
            if (it.wasRemoved()) {
                accountListViewFX.items.remove(it.valueRemoved)
            } else if (it.wasAdded()) {
                if (it.valueAdded.type != type.resourceLocation) {
                    return@profileWatchMapFX
                }
                accountListViewFX.items += it.valueAdded
            }
        }

        accountListViewFX.items.contains(selected).decide(selected, null).let {
            accountListViewFX.selectionModel.select(it)
            accountListViewFX.scrollTo(it)
            setAccountInfo(it)
        }
    }


    private fun setAccountInfo(account: Account?) {
        if (account == null) {
            accountInfoFX.children.clear()
            return
        }
        val profile = ErosProfileManager.selected.general.accountProfile

        val pane = GridPane()

        AnchorPane.setLeftAnchor(pane, 10.0)
        AnchorPane.setRightAnchor(pane, 10.0)


        GridPane().let {
            var row = 0

            for ((key, property) in ACCOUNT_INFO_PROPERTIES.extend(accountTypeListViewFX.selectionModel.selectedItem.additionalDetails)) { // ToDo
                val propertyValue = property(account) ?: continue

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
                setOnAction {
                    SimpleErosConfirmationDialog(
                        onConfirm = {
                            profile.entries -= account.id
                            if (profile.selected == account) {
                                profile.selected = null
                            }
                            JavaFXUtil.runLater { refreshList() }
                        }
                    ).show()
                }
            }, 1, 0)

            it.add(Button("Verify").apply {
                setOnAction {
                    isDisable = true
                    DefaultThreadPool += {
                        account.verify(profile.clientToken)
                        JavaFXUtil.runLater { refreshList() }
                    }
                }
            }, 3, 0)
            it.add(Button("Use").apply {
                setOnAction {
                    isDisable = true

                    DefaultThreadPool += {
                        account.verify(profile.clientToken) // ToDo: Show error
                        profile.selected = account
                        JavaFXUtil.runLater { refreshList() }
                    }
                }
                isDisable = profile.selected == account
            }, 4, 0)


            it.hgap = 5.0
            GridPane.setMargin(it, Insets(20.0, 0.0, 0.0, 0.0))

            pane.add(it, 0, 1)
        }


        accountInfoFX.children.setAll(pane)
    }

    companion object {
        val LAYOUT = "minosoft:eros/main/account/account.fxml".toResourceLocation()

        private val ACCOUNT_INFO_PROPERTIES: List<Pair<ResourceLocation, (account: Account) -> Any?>> = listOf(
            "minosoft:main.account.account_info.id".toResourceLocation() to { it.id },
        )

        val ACCOUNT_TYPES = listOf(
            ErosAccountType<MojangAccount>(
                resourceLocation = MojangAccount.RESOURCE_LOCATION,
                translationKey = "minosoft:main.account.type.mojang".toResourceLocation(),
                additionalDetails = listOf(
                    "minosoft:main.account.account_info.email".toResourceLocation() to { it.email },
                    "minosoft:main.account.account_info.uuid".toResourceLocation() to { it.uuid },
                ),
                icon = FontAwesomeSolid.BUILDING,
                addHandler = { MojangAddController(it).show() },
            ),
            ErosAccountType<OfflineAccount>(
                resourceLocation = OfflineAccount.RESOURCE_LOCATION,
                translationKey = "minosoft:main.account.type.offline".toResourceLocation(),
                icon = FontAwesomeSolid.MAP,
                addHandler = { OfflineAddController(it).show() },
            ),
            ErosAccountType<MicrosoftAccount>(
                resourceLocation = MicrosoftAccount.RESOURCE_LOCATION,
                translationKey = "minosoft:main.account.type.microsoft".toResourceLocation(),
                additionalDetails = listOf(
                    "minosoft:main.account.account_info.uuid".toResourceLocation() to { it.uuid },
                ),
                icon = FontAwesomeBrands.MICROSOFT,
                addHandler = { MicrosoftAddController(it).show() },
            ),
        )
    }
}
