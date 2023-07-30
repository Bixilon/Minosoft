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

package de.bixilon.minosoft.util.account

import de.bixilon.kutil.uuid.UUIDUtil.trim
import de.bixilon.minosoft.util.account.microsoft.minecraft.MinecraftAPIException
import de.bixilon.minosoft.util.account.microsoft.minecraft.MinecraftProfile
import de.bixilon.minosoft.util.account.minecraft.MinecraftPrivateKey
import de.bixilon.minosoft.util.account.minecraft.MinecraftTokens
import de.bixilon.minosoft.util.http.HTTP2.getJson
import de.bixilon.minosoft.util.http.HTTP2.postJson
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.*

object AccountUtil {
    private const val GET_PROFILE_URL = "https://api.minecraftservices.com/minecraft/profile"
    private const val MOJANG_URL_JOIN = "https://sessionserver.mojang.com/session/minecraft/join"
    private const val CERTIFICATE_URL = "https://api.minecraftservices.com/player/certificates"

    fun fetchMinecraftProfile(token: MinecraftTokens): MinecraftProfile {
        val response = GET_PROFILE_URL.getJson(mapOf(
            "Authorization" to "Bearer ${token.accessToken}",
        ))

        if (response.statusCode != 200) {
            throw MinecraftAPIException(response) // 404 means that the account has not purchased minecraft
        }

        return Jackson.MAPPER.convertValue(response.body, MinecraftProfile::class.java)
    }

    fun joinMojangServer(accessToken: String, profile: UUID, serverId: String) {
        val response = mutableMapOf(
            "accessToken" to accessToken,
            "selectedProfile" to profile.trim(),
            "serverId" to serverId,
        ).postJson(MOJANG_URL_JOIN)


        if (response.statusCode != 204 && response.statusCode != 200) {
            throw MinecraftAPIException(response)
        }

        Log.log(LogMessageType.AUTHENTICATION, LogLevels.VERBOSE) { "Mojang server join successful (profile=$profile, serverId=$serverId)" }
    }

    fun fetchPrivateKey(token: MinecraftTokens): MinecraftPrivateKey {
        val response = emptyMap<String, Any>().postJson(CERTIFICATE_URL, mapOf(
            "Authorization" to "Bearer ${token.accessToken}",
        ))

        if (response.statusCode != 200) {
            throw MinecraftAPIException(response)
        }

        return Jackson.MAPPER.convertValue(response.body, MinecraftPrivateKey::class.java)
    }
}
