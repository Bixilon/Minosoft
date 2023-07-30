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

package de.bixilon.minosoft.data.accounts.types.offline

import com.fasterxml.jackson.annotation.JsonIgnore
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.accounts.AccountStates
import de.bixilon.minosoft.data.entities.entities.player.properties.PlayerProperties
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import java.util.*

class OfflineAccount(username: String) : Account(username) {
    override val id: String = username
    override val uuid: UUID = UUID("OfflinePlayer:$username".hashCode().toLong(), 0L) // ToDo
    override val type: ResourceLocation = identifier
    override var state: AccountStates
        get() = AccountStates.WORKING
        set(value) {}

    override val supportsEncryption: Boolean get() = false
    override val supportsSkins: Boolean get() = false

    @JsonIgnore
    override val properties: PlayerProperties = PlayerProperties()

    override fun join(serverId: String) = Unit

    override fun logout(clientToken: String) = Unit

    override fun check(latch: AbstractLatch?, clientToken: String) = Unit

    override fun toString(): String {
        return "OfflineAccount{$username}"
    }

    companion object : Identified {
        override val identifier: ResourceLocation = "minosoft:offline_account".toResourceLocation()
    }
}
