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

package de.bixilon.minosoft.util.account

import de.bixilon.minosoft.data.accounts.MojangAccountInfo
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.nullCast
import de.bixilon.minosoft.util.KUtil.trim
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.http.HTTP2.getJson
import de.bixilon.minosoft.util.http.HTTP2.postJson
import de.bixilon.minosoft.util.http.exceptions.AuthenticationException
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

object AccountUtil {
    private const val MOJANG_URL_JOIN = "https://sessionserver.mojang.com/session/minecraft/join"

    fun getMojangAccountInfo(bearerToken: String): MojangAccountInfo {
        val response = ProtocolDefinition.MICROSOFT_ACCOUNT_GET_MOJANG_PROFILE_URL.getJson(mapOf(
            "Authorization" to "Bearer $bearerToken"
        ))

        response.body!!
        if (response.statusCode != 200) {
            val errorMessage = when (response.statusCode) {
                404 -> "You don't have a copy of minecraft!"
                else -> response.body["errorMessage"].unsafeCast()
            }
            throw LoginException(response.statusCode, "Could not get minecraft profile", errorMessage)
        }

        return MojangAccountInfo(
            id = response.body["id"].unsafeCast(),
            name = response.body["name"].unsafeCast(),
        )
    }

    fun joinMojangServer(username: String, accessToken: String, selectedProfile: UUID, serverId: String) {
        val response = mutableMapOf(
            "accessToken" to accessToken,
            "selectedProfile" to selectedProfile.trim(),
            "serverId" to serverId,
        ).postJson(MOJANG_URL_JOIN)


        if (response.statusCode != 204) {
            response.body!!
            throw AuthenticationException(response.statusCode, response.body["errorMessage"]?.nullCast())
        }

        Log.log(LogMessageType.AUTHENTICATION, LogLevels.VERBOSE) { "Mojang server join successful (username=$username, serverId=$serverId)" }
    }
}
