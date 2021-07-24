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

import com.google.gson.JsonParser
import de.bixilon.minosoft.data.accounts.types.MicrosoftAccount
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.HTTP
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.logging.Log
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler

object MicrosoftOAuthUtils {
    val NULL_URL_CONNECTION: URLConnection = object : URLConnection(null) {
        override fun connect() {}
    }

    fun loginToMicrosoftAccount(authorizationCode: String) {
        Log.verbose("Logging into microsoft account...")
        try {
            val authorizationToken = getAuthorizationToken(authorizationCode)
            val xboxLiveToken = getXboxLiveToken(authorizationToken)
            val xstsToken = getXSTSToken(xboxLiveToken.first)

            val microsoftAccount = getMicrosoftAccount(getMinecraftAccessToken(xboxLiveToken.second, xstsToken))
            // ToDo: Account.addAccount(microsoftAccount)
        } catch (exception: Exception) {
            Log.warn("Can not login into microsoft account")
            exception.printStackTrace()

            if (RunConfiguration.DISABLE_EROS) {
                return
            }

            var message = "Could not login!"
            var errorMessage = exception.javaClass.canonicalName + ": " + exception.message
            if (exception is LoginException) {
                message = "${exception.message} (${exception.errorCode})"
                errorMessage = exception.errorMessage
            }

            //   Platform.runLater {
            //       val dialog = JFXAlert<Boolean>()
            //       // ToDo: GUITools.initializePane(dialog.dialogPane)
            //       // Do not translate this, translations might fail to load...
            //       dialog.title = "Login error"
            //       val layout = JFXDialogLayout()
            //       layout.setHeading(Text(message))
            //       val text = TextArea(errorMessage)
            //       text.isEditable = false
            //       text.isWrapText = true
            //       layout.setBody(text)
            //       dialog.dialogPane.content = layout
            //       val stage = dialog.dialogPane.scene.window as Stage
            //       stage.toFront()
            //       dialog.show()
            //   }
        }
    }

    fun getAuthorizationToken(authorizationCode: String): String {
        val data = mapOf(
            "client_id" to ProtocolDefinition.MICROSOFT_ACCOUNT_APPLICATION_ID,
            "code" to authorizationCode,
            "grant_type" to "authorization_code",
            "scope" to "service::user.auth.xboxlive.com::MBI_SSL",
        )
        val response = HTTP.postData(ProtocolDefinition.MICROSOFT_ACCOUNT_AUTH_TOKEN_URL, HashMap(data))
        if (response.statusCode() != 200) {
            throw LoginException(response.statusCode(), "Could not get authorization token ", response.body())
        }
        val body = JsonParser.parseString(response.body()).asJsonObject
        return body["access_token"]!!.asString
    }

    /**
     * returns A: XBL Token; B: UHS Token
     */
    fun getXboxLiveToken(authorizationToken: String): Pair<String, String> {
        val payload = mapOf(
            "Properties" to mapOf(
                "AuthMethod" to "RPS",
                "SiteName" to "user.auth.xboxlive.com",
                "RpsTicket" to authorizationToken
            ),
            "RelyingParty" to "http://auth.xboxlive.com",
            "TokenType" to "JWT",
        )
        val response = HTTP.postJson(ProtocolDefinition.MICROSOFT_ACCOUNT_XBOX_LIVE_AUTHENTICATE_URL, Util.GSON.toJson(payload))

        if (response.statusCode() != 200) {
            throw LoginException(response.statusCode(), "Could not get authenticate against xbox live ", response.body())
        }
        val body = JsonParser.parseString(response.body()).asJsonObject
        return Pair(body["Token"]!!.asString, body["DisplayClaims"].asJsonObject["xui"].asJsonArray[0].asJsonObject["uhs"].asString)
    }

    fun getXSTSToken(xBoxLiveToken: String): String {
        val payload = mapOf(
            "Properties" to mapOf(
                "SandboxId" to "RETAIL",
                "UserTokens" to listOf(xBoxLiveToken)
            ),
            "RelyingParty" to "rp://api.minecraftservices.com/",
            "TokenType" to "JWT",
        )
        val response = HTTP.postJson(ProtocolDefinition.MICROSOFT_ACCOUNT_XSTS_URL, Util.GSON.toJson(payload))

        if (response.statusCode() != 200) {
            val error = JsonParser.parseString(response.body()).asJsonObject
            val errorMessage = when (error["XErr"].asLong) {
                2148916233 -> "You don't have an XBox account!"
                2148916238 -> "This account is a child account!"
                else -> error["Message"].asString
            }
            throw LoginException(response.statusCode(), "Could not get authenticate against XSTS token ", errorMessage)
        }
        val body = JsonParser.parseString(response.body()).asJsonObject
        return body["Token"].asString!!
    }

    fun getMinecraftAccessToken(uhs: String, xstsToken: String): String {
        val payload = mapOf(
            "identityToken" to "XBL3.0 x=${uhs};${xstsToken}"
        )
        val response = HTTP.postJson(ProtocolDefinition.MICROSOFT_ACCOUNT_MINECRAFT_LOGIN_WITH_XBOX_URL, Util.GSON.toJson(payload))

        if (response.statusCode() != 200) {
            val error = JsonParser.parseString(response.body()).asJsonObject
            throw LoginException(response.statusCode(), "Could not get minecraft access token ", error["errorMessage"].asString)
        }
        val body = JsonParser.parseString(response.body()).asJsonObject
        return body["access_token"].asString!!
    }

    fun getMicrosoftAccount(bearerToken: String): MicrosoftAccount {
        val response = HTTP.get(ProtocolDefinition.MICROSOFT_ACCOUNT_GET_MOJANG_PROFILE_URL, HashMap(mapOf(
            "Authorization" to "Bearer $bearerToken"
        )))

        if (response.statusCode() != 200) {
            val errorMessage = when (response.statusCode()) {
                404 -> "You don't have a copy of minecraft!"
                else -> JsonParser.parseString(response.body()).asJsonObject["errorMessage"].asString
            }
            throw LoginException(response.statusCode(), "Could not get minecraft profile", errorMessage)
        }

        val body = JsonParser.parseString(response.body()).asJsonObject
        // return MicrosoftAccount(bearerToken, body["id"].asString!!, Util.getUUIDFromString(body["id"].asString!!), body["name"].asString!!)
        TODO()
    }

    init {
        URL.setURLStreamHandlerFactory {
            if (it == "ms-xal-" + ProtocolDefinition.MICROSOFT_ACCOUNT_APPLICATION_ID) {
                return@setURLStreamHandlerFactory object : URLStreamHandler() {
                    override fun openConnection(url: URL): URLConnection {
                        loginToMicrosoftAccount(Util.urlQueryToMap(url.query)["code"]!!)
                        return NULL_URL_CONNECTION
                    }
                }
            }
            return@setURLStreamHandlerFactory null
        }
    }
}
