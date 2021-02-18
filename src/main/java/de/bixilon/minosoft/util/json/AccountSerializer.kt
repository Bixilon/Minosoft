/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.accounts.MicrosoftAccount
import de.bixilon.minosoft.data.accounts.MojangAccount
import de.bixilon.minosoft.data.accounts.OfflineAccount

class AccountSerializer {
    @FromJson
    fun fromJson(json: Map<String, Any>): Account {
        return when (json["type"]!!) {
            "mojang" -> MojangAccount.deserialize(json)
            "offline" -> OfflineAccount.deserialize(json)
            "microsoft" -> MicrosoftAccount.deserialize(json)
            else -> throw IllegalArgumentException("Invalid account type: ${json["type"]}")
        }
    }

    @ToJson
    fun toJson(account: Account): Map<String, Any> {
        return account.serialize()
    }
}
