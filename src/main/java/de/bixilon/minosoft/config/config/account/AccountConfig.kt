/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config.config.account

import com.squareup.moshi.Json
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.modding.event.events.account.AccountSelectEvent
import java.util.*

data class AccountConfig(
    @Json(name = "selected") var selectedAccountId: String? = null,
    @Json(name = "client_token") var clientToken: String = UUID.randomUUID().toString(),
    val entries: MutableMap<String, Account> = mutableMapOf(),
) {
    @Transient
    var selected: Account? = null
        get() = entries[selectedAccountId]
        set(value) {
            Minosoft.GLOBAL_EVENT_MASTER.fireEvent(AccountSelectEvent(selected, value))
            field // To allow transient for moshi
            selectedAccountId = value?.id
        }
}
