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

package de.bixilon.minosoft.data.accounts

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.Server
import de.bixilon.minosoft.data.accounts.types.MicrosoftAccount
import de.bixilon.minosoft.data.accounts.types.MojangAccount
import de.bixilon.minosoft.data.accounts.types.OfflineAccount
import de.bixilon.minosoft.data.player.properties.PlayerProperties
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = MojangAccount::class, name = "minosoft:mojang_account"),
    JsonSubTypes.Type(value = OfflineAccount::class, name = "minosoft:offline_account"),
    JsonSubTypes.Type(value = MicrosoftAccount::class, name = "minosoft:microsoft_account"),
)
abstract class Account(
    val username: String,
) {
    abstract val id: String
    abstract val type: ResourceLocation
    abstract val properties: PlayerProperties?

    @Transient
    @JsonIgnore
    val connections: MutableMap<Server, PlayConnection> = synchronizedMapOf()

    abstract fun join(serverId: String)

    abstract fun logout(clientToken: String)
    abstract fun verify(clientToken: String)
}
