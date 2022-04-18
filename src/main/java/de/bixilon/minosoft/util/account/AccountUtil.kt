/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kutil.uuid.UUIDUtil.trim
import de.bixilon.minosoft.data.accounts.types.microsoft.MinecraftTokens
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.account.microsoft.minecraft.MinecraftAPIException
import de.bixilon.minosoft.util.account.microsoft.minecraft.MinecraftProfile
import de.bixilon.minosoft.util.http.HTTP2.getJson
import de.bixilon.minosoft.util.http.HTTP2.postJson
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

object AccountUtil {
    private const val MOJANG_URL_JOIN = "https://sessionserver.mojang.com/session/minecraft/join"

    fun fetchMinecraftProfile(token: MinecraftTokens): MinecraftProfile {
        val response = ProtocolDefinition.MICROSOFT_ACCOUNT_GET_MOJANG_PROFILE_URL.getJson(mapOf(
            "Authorization" to "Bearer ${token.accessToken}",
        ))

        if (response.statusCode != 200) {
            throw MinecraftAPIException(response) // 404 means that the account has not purchased minecraft
        }

        return Jackson.MAPPER.convertValue(response.body, MinecraftProfile::class.java)
    }

    fun joinMojangServer(username: String, accessToken: String, selectedProfile: UUID, serverId: String) {
        val response = mutableMapOf(
            "accessToken" to accessToken,
            "selectedProfile" to selectedProfile.trim(),
            "serverId" to serverId,
        ).postJson(MOJANG_URL_JOIN)


        if (response.statusCode != 204 && response.statusCode != 200) {
            throw MinecraftAPIException(response)
        }

        Log.log(LogMessageType.AUTHENTICATION, LogLevels.VERBOSE) { "Mojang server join successful (username=$username, serverId=$serverId)" }
    }
}
