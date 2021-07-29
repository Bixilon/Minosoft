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
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.accounts.types.MicrosoftAccount
import de.bixilon.minosoft.data.accounts.types.MojangAccount
import de.bixilon.minosoft.data.accounts.types.OfflineAccount
import de.bixilon.minosoft.gui.eros.controller.EmbeddedJavaFXController
import de.bixilon.minosoft.util.KUtil.asResourceLocation
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane
import org.kordamp.ikonli.fontawesome5.FontAwesomeBrands
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid

class AccountController : EmbeddedJavaFXController<Pane>() {
    @FXML private lateinit var accountTypeListViewFX: ListView<ErosAccountType<*>>

    @FXML private lateinit var accountListViewFX: ListView<Account>
    @FXML private lateinit var accountInfoFX: AnchorPane


    override fun init() {
        accountTypeListViewFX.setCellFactory { AccountTypeCardController.build() }
        accountTypeListViewFX.items += ACCOUNT_TYPES

        accountListViewFX.setCellFactory { AccountCardController.build() }

        accountTypeListViewFX.selectionModel.select(0)

        for (account in Minosoft.config.config.account.entries.values) {
            if (account.type != accountTypeListViewFX.selectionModel.selectedItem.resourceLocation) {
                continue
            }
            accountListViewFX.items += account
        }
    }

    companion object {
        val LAYOUT = "minosoft:eros/main/account/account.fxml".asResourceLocation()

        val ACCOUNT_TYPES = listOf(
            ErosAccountType<MojangAccount>(
                resourceLocation = MojangAccount.RESOURCE_LOCATION,
                translationKey = "minosoft:main.account.type.mojang".asResourceLocation(),
                icon = FontAwesomeSolid.BUILDING,
                addHandler = { TODO() }
            ),
            ErosAccountType<OfflineAccount>(
                resourceLocation = OfflineAccount.RESOURCE_LOCATION,
                translationKey = "minosoft:main.account.type.offline".asResourceLocation(),
                icon = FontAwesomeSolid.MAP,
                addHandler = { TODO() }
            ),
            ErosAccountType<MicrosoftAccount>(
                resourceLocation = MicrosoftAccount.RESOURCE_LOCATION,
                translationKey = "minosoft:main.account.type.microsoft".asResourceLocation(),
                icon = FontAwesomeBrands.MICROSOFT,
                addHandler = { TODO() }
            ),
        )
    }
}
