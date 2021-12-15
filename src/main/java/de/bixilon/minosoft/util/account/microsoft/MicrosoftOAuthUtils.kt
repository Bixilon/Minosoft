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

package de.bixilon.minosoft.util.account.microsoft

import de.bixilon.minosoft.data.accounts.types.MicrosoftAccount
import de.bixilon.minosoft.data.player.properties.PlayerProperties
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.KUtil.asList
import de.bixilon.minosoft.util.KUtil.asUUID
import de.bixilon.minosoft.util.KUtil.toLong
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.account.AccountUtil
import de.bixilon.minosoft.util.account.LoginException
import de.bixilon.minosoft.util.http.HTTP2.postData
import de.bixilon.minosoft.util.http.HTTP2.postJson
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.asCompound
import de.bixilon.minosoft.util.url.URLProtocolStreamHandlers
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler

object MicrosoftOAuthUtils {

    fun loginToMicrosoftAccount(authorizationCode: String): MicrosoftAccount {
        Log.log(LogMessageType.AUTHENTICATION, LogLevels.INFO) { "Logging into microsoft account..." }
        val authorizationToken = getAuthorizationToken(authorizationCode)
        val (xboxLiveToken, userHash) = getXboxLiveToken(authorizationToken)
        val xstsToken = getXSTSToken(xboxLiveToken)

        val accessToken = getMinecraftBearerAccessToken(userHash, xstsToken)
        val accountInfo = AccountUtil.getMojangAccountInfo(accessToken)

        val uuid = accountInfo.id.asUUID()
        val account = MicrosoftAccount(
            uuid = uuid,
            username = accountInfo.name,
            authorizationToken = authorizationToken,
            properties = PlayerProperties.fetch(uuid),
        )

        account.accessToken = accessToken
        account.verify("")

        Log.log(LogMessageType.AUTHENTICATION, LogLevels.INFO) { "Microsoft account login successful (uuid=${account.uuid})" }

        return account
    }

    fun getAuthorizationToken(authorizationCode: String): String {
        val response = mapOf(
            "client_id" to ProtocolDefinition.MICROSOFT_ACCOUNT_APPLICATION_ID,
            "code" to authorizationCode,
            "grant_type" to "authorization_code",
            "scope" to "service::user.auth.xboxlive.com::MBI_SSL",
        ).postData(ProtocolDefinition.MICROSOFT_ACCOUNT_AUTH_TOKEN_URL)
        if (response.statusCode != 200) {
            throw LoginException(response.statusCode, "Could not get authorization token", response.body.toString())
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
            throw LoginException(response.statusCode, "Could not authenticate with xbox live token", response.body.toString())
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
            throw LoginException(response.statusCode, "Could not get xsts token", errorMessage)
        }
        return response.body["Token"].unsafeCast()
    }

    fun getMinecraftBearerAccessToken(userHash: String, xstsToken: String): String {
        val response = mapOf(
            "identityToken" to "XBL3.0 x=${userHash};${xstsToken}",
            "ensureLegacyEnabled" to true,
        ).postJson(ProtocolDefinition.MICROSOFT_ACCOUNT_MINECRAFT_LOGIN_WITH_XBOX_URL)

        response.body!!
        if (response.statusCode != 200) {
            throw LoginException(response.statusCode, "Could not get minecraft access token ", (response.body["errorMessage"] ?: response.body["error"] ?: "unknown").unsafeCast())
        }
        return response.body["access_token"].unsafeCast()
    }


    init {
        URLProtocolStreamHandlers.PROTOCOLS["ms-xal-" + ProtocolDefinition.MICROSOFT_ACCOUNT_APPLICATION_ID] = LoginURLHandler
    }

    private object LoginURLHandler : URLStreamHandler() {

        override fun openConnection(url: URL): URLConnection {
            return URLProtocolStreamHandlers.NULL_URL_CONNECTION
        }
    }
}
