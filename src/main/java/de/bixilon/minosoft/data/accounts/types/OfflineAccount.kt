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

package de.bixilon.minosoft.data.accounts.types

import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.accounts.AccountType
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class OfflineAccount(username: String) : Account(username) {
    override val id: String = username
    override val type: ResourceLocation = RESOURCE_LOCATION

    override fun join(serverId: String) = Unit

    override fun logout(clientToken: String) = Unit

    override fun verify(clientToken: String) = Unit

    override fun toString(): String {
        return "OfflineAccount{$username}"
    }

    companion object : AccountType(OfflineAccount::class) {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:offline_account".toResourceLocation()
    }
}
