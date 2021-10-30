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

import com.squareup.moshi.Json
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.accounts.AccountType
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.account.AccountUtil
import de.bixilon.minosoft.util.account.microsoft.MicrosoftOAuthUtils
import java.util.*

class MicrosoftAccount(
    val uuid: UUID,
    username: String,
    @Json(name = "authorization_token") private val authorizationToken: String,
) : Account(username) {
    @Transient var accessToken: String? = null
    override val id: String = uuid.toString()
    override val type: ResourceLocation = RESOURCE_LOCATION

    override fun join(serverId: String) {
        AccountUtil.joinMojangServer(username, accessToken!!, uuid, serverId)
    }

    override fun logout() = Unit

    override fun verify() {
        if (accessToken != null) {
            return
        }
        val (xboxLiveToken, userHash) = MicrosoftOAuthUtils.getXboxLiveToken(authorizationToken)
        val xstsToken = MicrosoftOAuthUtils.getXSTSToken(xboxLiveToken)

        accessToken = MicrosoftOAuthUtils.getMinecraftBearerAccessToken(userHash, xstsToken)
    }

    override fun serialize(): Map<String, Any> {
        return mapOf(
            "uuid" to uuid,
            "username" to username,
            "authorization_token" to authorizationToken,
            "type" to type,
        )
    }

    companion object : AccountType(MicrosoftAccount::class) {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:microsoft_account".toResourceLocation()
    }
}
