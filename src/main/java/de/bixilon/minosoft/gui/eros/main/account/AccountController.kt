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

package de.bixilon.minosoft.gui.eros.main.account

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.CollectionUtil.extend
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.observer.map.MapChange.Companion.values
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.accounts.AccountStates
import de.bixilon.minosoft.data.accounts.types.microsoft.MicrosoftAccount
import de.bixilon.minosoft.data.accounts.types.mojang.MojangAccount
import de.bixilon.minosoft.data.accounts.types.offline.OfflineAccount
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TranslatableComponents
import de.bixilon.minosoft.gui.eros.controller.EmbeddedJavaFXController
import de.bixilon.minosoft.gui.eros.dialog.ErosErrorReport.Companion.report
import de.bixilon.minosoft.gui.eros.dialog.SimpleErosConfirmationDialog
import de.bixilon.minosoft.gui.eros.main.account.add.MicrosoftAddController
import de.bixilon.minosoft.gui.eros.main.account.add.MojangAddController
import de.bixilon.minosoft.gui.eros.main.account.add.OfflineAddController
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.ctext
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.delegate.JavaFXDelegate.observeMapFX
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
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
        addButtonFX.ctext = ADD


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
            if (account.type != type.identifier) {
                continue
            }
            accountListViewFX.items += account
        }
        profile::entries.observeMapFX(this) {
            accountListViewFX.items.removeAll(it.removes.values())
            for ((_, value) in it.adds) {
                if (value.type != type.identifier) {
                    continue
                }
                accountListViewFX.items += value
            }
        }

        accountListViewFX.items.contains(selected).decide(selected, null).let {
            accountListViewFX.selectionModel.select(it)
            accountListViewFX.scrollTo(it)
            setAccountInfo(it)
        }
    }


    fun checkAccount(account: Account, select: Boolean, checkOnly: Boolean = false, onSuccess: ((Account) -> Unit)? = null) {
        val profile = ErosProfileManager.selected.general.accountProfile
        if (account.state == AccountStates.WORKING) {
            onSuccess?.let {
                DefaultThreadPool += {
                    it(account)
                }
            }

            if (select) {
                profile.selected = account
            }
            return
        }
        if (account.state == AccountStates.CHECKING || account.state == AccountStates.REFRESHING) {
            return
        }
        Log.log(LogMessageType.AUTHENTICATION, LogLevels.INFO) { "Checking account $account" }
        val latch = CountUpAndDownLatch(2)
        val dialog = CheckingDialog(latch)
        dialog.show()
        DefaultThreadPool += {
            latch.dec()
            try {
                account.tryCheck(latch, profile.clientToken) // ToDo: Show error
                if (select) {
                    profile.selected = account
                }
                if (account.state == AccountStates.WORKING) {
                    Log.log(LogMessageType.AUTHENTICATION, LogLevels.INFO) { "Account is working: $account" }
                }
                JavaFXUtil.runLater { dialog.close() }
                onSuccess?.invoke(account)
            } catch (exception: Throwable) {
                JavaFXUtil.runLater { dialog.close() }
                Log.log(LogMessageType.AUTHENTICATION, LogLevels.INFO) { "Error while checking account $account: $exception" }
                exception.printStackTrace()
                if (account.state == AccountStates.ERRORED || account.state == AccountStates.EXPIRED) {
                    val refreshHandler = account.erosType?.refreshHandler
                    if (refreshHandler == null || checkOnly) {
                        exception.report()
                    } else {
                        refreshHandler(this, account)
                    }
                }
            }
            JavaFXUtil.runLater { refreshList() }
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
                    SimpleErosConfirmationDialog(onConfirm = {
                        if (profile.selected == account) {
                            profile.selected = null
                        }
                        profile.entries -= account.id
                        JavaFXUtil.runLater { refreshList() }
                    }).show()
                }
                ctext = TranslatableComponents.GENERAL_DELETE
            }, 1, 0)

            it.add(Button("Check").apply {
                setOnAction {
                    isDisable = true
                    checkAccount(account, false, true)
                }
                ctext = CHECK
                if (account.state == AccountStates.WORKING || account.state == AccountStates.CHECKING || account.state == AccountStates.REFRESHING) {
                    isDisable = true
                }
            }, 3, 0)
            it.add(Button("Use").apply {
                setOnAction {
                    isDisable = true
                    checkAccount(account, true)
                }
                isDisable = profile.selected == account
                ctext = USE
            }, 4, 0)


            it.hgap = 5.0
            GridPane.setMargin(it, Insets(20.0, 0.0, 0.0, 0.0))

            pane.add(it, 0, 1)
        }


        accountInfoFX.children.setAll(pane)
    }

    companion object {
        val LAYOUT = "minosoft:eros/main/account/account.fxml".toResourceLocation()

        private val CHECK = "minosoft:main.account.list.info.button.check".toResourceLocation()
        private val USE = "minosoft:main.account.list.info.button.use".toResourceLocation()
        private val ADD = "minosoft:main.account.list.info.button.add".toResourceLocation()

        private val ACCOUNT_INFO_PROPERTIES: List<Pair<ResourceLocation, (account: Account) -> Any?>> = listOf(
            "minosoft:main.account.account_info.id".toResourceLocation() to { it.id },
            "minosoft:main.account.account_info.state".toResourceLocation() to { it.state },
        )

        val ACCOUNT_TYPES = listOf(
            ErosAccountType<MicrosoftAccount>(
                identifier = MicrosoftAccount.identifier,
                translationKey = "minosoft:main.account.type.microsoft".toResourceLocation(),
                additionalDetails = listOf(
                    "minosoft:main.account.account_info.uuid".toResourceLocation() to { it.uuid },
                ),
                icon = FontAwesomeBrands.MICROSOFT,
                addHandler = { MicrosoftAddController(it).request() },
                refreshHandler = { controller, account -> MicrosoftAddController(controller, account).request() }
            ),
            ErosAccountType<OfflineAccount>(
                identifier = OfflineAccount.identifier,
                translationKey = "minosoft:main.account.type.offline".toResourceLocation(),
                icon = FontAwesomeSolid.MAP,
                addHandler = { OfflineAddController(it).show() },
            ),
            ErosAccountType<MojangAccount>(
                identifier = MojangAccount.identifier,
                translationKey = "minosoft:main.account.type.mojang".toResourceLocation(),
                additionalDetails = listOf(
                    "minosoft:main.account.account_info.email".toResourceLocation() to { it.email },
                    "minosoft:main.account.account_info.uuid".toResourceLocation() to { it.uuid },
                ),
                icon = FontAwesomeSolid.BUILDING,
                addHandler = { MojangAddController(it).show() },
            ),
        )

        val <T : Account>T.erosType: ErosAccountType<T>?
            get() {
                for (type in ACCOUNT_TYPES) {
                    if (type.identifier == this.type) {
                        return type.unsafeCast()
                    }
                }
                return null
            }
    }
}
