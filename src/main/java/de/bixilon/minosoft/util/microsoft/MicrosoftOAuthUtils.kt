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

package de.bixilon.minosoft.util.microsoft

import de.bixilon.minosoft.data.accounts.types.MicrosoftAccount
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.asList
import de.bixilon.minosoft.util.KUtil.toLong
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.http.HTTP2.getJson
import de.bixilon.minosoft.util.http.HTTP2.postJson
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.asCompound
import java.net.URLConnection

object MicrosoftOAuthUtils {
    val NULL_URL_CONNECTION: URLConnection = object : URLConnection(null) {
        override fun connect() {}
    }

    fun loginToMicrosoftAccount(authorizationCode: String): MicrosoftAccount {
        Log.log(LogMessageType.AUTHENTICATION, LogLevels.INFO) { "Logging into microsoft account..." }
        val authorizationToken = getAuthorizationToken(authorizationCode)
        val xboxLiveToken = getXboxLiveToken(authorizationToken)
        val xstsToken = getXSTSToken(xboxLiveToken.first)

        return getMicrosoftAccount(getMinecraftAccessToken(xboxLiveToken.second, xstsToken))
    }

    fun getAuthorizationToken(authorizationCode: String): String {
        val response = mapOf(
            "client_id" to ProtocolDefinition.MICROSOFT_ACCOUNT_APPLICATION_ID,
            "code" to authorizationCode,
            "grant_type" to "authorization_code",
            "scope" to "service::user.auth.xboxlive.com::MBI_SSL",
        ).postJson(ProtocolDefinition.MICROSOFT_ACCOUNT_AUTH_TOKEN_URL)
        if (response.statusCode != 200) {
            throw LoginException(response.statusCode, "Could not get authorization token ", response.body.toString())
        }
        response.body!!
        return response.body["access_token"].unsafeCast()
    }

    /**
     * returns A: XBL Token; B: UHS Token
     */
    fun getXboxLiveToken(authorizationToken: String): Pair<String, String> {
        val response = mapOf(
            "Properties" to mapOf(
                "AuthMethod" to "RPS",
                "SiteName" to "user.auth.xboxlive.com",
                "RpsTicket" to authorizationToken
            ),
            "RelyingParty" to "http://auth.xboxlive.com",
            "TokenType" to "JWT",
        ).postJson(ProtocolDefinition.MICROSOFT_ACCOUNT_XBOX_LIVE_AUTHENTICATE_URL)

        response.body!!
        if (response.statusCode != 200) {
            throw LoginException(response.statusCode, "Could not get authenticate against xbox live ", response.body.toString())
        }
        return Pair(response.body["Token"].unsafeCast(), response.body["DisplayClaims"].asCompound()["xui"].asList()[0].asCompound()["uhs"].unsafeCast())
    }

    fun getXSTSToken(xBoxLiveToken: String): String {
        val response = mapOf(
            "Properties" to mapOf(
                "SandboxId" to "RETAIL",
                "UserTokens" to listOf(xBoxLiveToken)
            ),
            "RelyingParty" to "rp://api.minecraftservices.com/",
            "TokenType" to "JWT",
        ).postJson(ProtocolDefinition.MICROSOFT_ACCOUNT_XSTS_URL)

        response.body!!
        if (response.statusCode != 200) {
            val errorMessage = when (response.body["XErr"].toLong()) {
                2148916233 -> "You don't have an XBox account!"
                2148916238 -> "This account is a child account!"
                else -> response.body["Message"].unsafeCast()
            }
            throw LoginException(response.statusCode, "Could not get authenticate against XSTS token ", errorMessage)
        }
        return response.body["Token"].unsafeCast()
    }

    fun getMinecraftAccessToken(uhs: String, xstsToken: String): String {
        val response = mapOf(
            "identityToken" to "XBL3.0 x=${uhs};${xstsToken}"
        ).postJson(ProtocolDefinition.MICROSOFT_ACCOUNT_MINECRAFT_LOGIN_WITH_XBOX_URL)

        response.body!!
        if (response.statusCode != 200) {
            throw LoginException(response.statusCode, "Could not get minecraft access token ", response.body["errorMessage"].unsafeCast())
        }
        return response.body["access_token"].unsafeCast()
    }

    fun getMicrosoftAccount(bearerToken: String): MicrosoftAccount {
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

        // return MicrosoftAccount(bearerToken, body["id"].asString!!, Util.getUUIDFromString(body["id"].asString!!), body["name"].asString!!)
        TODO()
    }

    init {
        // ToDo
//        URL.setURLStreamHandlerFactory {
//            if (it == "ms-xal-" + ProtocolDefinition.MICROSOFT_ACCOUNT_APPLICATION_ID) {
//                return@setURLStreamHandlerFactory object : URLStreamHandler() {
//                    override fun openConnection(url: URL): URLConnection {
//                        loginToMicrosoftAccount(Util.urlQueryToMap(url.query)["code"]!!)
//                        return NULL_URL_CONNECTION
//                    }
//                }
//            }
//            return@setURLStreamHandlerFactory null
//        }
    }
}
