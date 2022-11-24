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

package de.bixilon.minosoft.util.account.microsoft

import de.bixilon.kutil.concurrent.pool.DefaultThreadPool
import de.bixilon.kutil.concurrent.time.TimeWorker.runLater
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.data.accounts.AccountStates
import de.bixilon.minosoft.data.accounts.types.microsoft.MicrosoftAccount
import de.bixilon.minosoft.data.accounts.types.microsoft.MicrosoftTokens
import de.bixilon.minosoft.data.entities.entities.player.properties.PlayerProperties
import de.bixilon.minosoft.util.account.AccountUtil
import de.bixilon.minosoft.util.account.microsoft.code.MicrosoftDeviceCode
import de.bixilon.minosoft.util.account.microsoft.minecraft.MinecraftAPIException
import de.bixilon.minosoft.util.account.microsoft.minecraft.MinecraftBearerResponse
import de.bixilon.minosoft.util.account.microsoft.xbox.XSTSToken
import de.bixilon.minosoft.util.account.microsoft.xbox.XboxAPIError
import de.bixilon.minosoft.util.account.microsoft.xbox.XboxAPIException
import de.bixilon.minosoft.util.account.microsoft.xbox.XboxLiveToken
import de.bixilon.minosoft.util.http.HTTP2.postData
import de.bixilon.minosoft.util.http.HTTP2.postJson
import de.bixilon.minosoft.util.json.Jackson
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.concurrent.TimeoutException

object MicrosoftOAuthUtils {
    const val CLIENT_ID = "feb3836f-0333-4185-8eb9-4cbf0498f947" // Minosoft 2 (microsoft-bixilon2)
    const val TENANT = "consumers"

    const val DEVICE_CODE_URL = "https://login.microsoftonline.com/$TENANT/oauth2/v2.0/devicecode"
    const val TOKEN_CHECK_URL = "https://login.microsoftonline.com/$TENANT/oauth2/v2.0/token"
    const val XBOX_LIVE_AUTH_URL = "https://user.auth.xboxlive.com/user/authenticate"
    const val XSTS_URL = "https://xsts.auth.xboxlive.com/xsts/authorize"
    const val LOGIN_WITH_XBOX_URL = "https://api.minecraftservices.com/authentication/login_with_xbox"
    const val MAX_CHECK_TIME = 900

    fun obtainDeviceCodeAsync(
        deviceCodeCallback: (MicrosoftDeviceCode) -> Unit,
        errorCallback: (Throwable) -> Unit,
        successCallback: (AuthenticationResponse) -> Unit,
    ) {
        DefaultThreadPool += {
            val deviceCode = obtainDeviceCode()
            Log.log(LogMessageType.AUTHENTICATION, LogLevels.INFO) { "Obtained device code: ${deviceCode.userCode}" }
            deviceCodeCallback(deviceCode)
            val start = TimeUtil.millis / 1000

            fun checkToken() {
                try {
                    val response = checkDeviceCode(deviceCode)
                    val time = TimeUtil.millis / 1000
                    if (time > start + MAX_CHECK_TIME || time > deviceCode.expires) {
                        throw TimeoutException("Could not obtain access for device code: ${deviceCode.userCode}")
                    }
                    if (response == null) {
                        // no response yet
                        runLater(deviceCode.interval * 1000) { checkToken() }
                        return
                    }
                    Log.log(LogMessageType.AUTHENTICATION, LogLevels.INFO) { "Code (${deviceCode.userCode}) is valid, logging in..." }
                    successCallback(response)
                } catch (exception: Throwable) {
                    exception.printStackTrace()
                    errorCallback(exception)
                }
            }
            checkToken()
        }
    }

    fun obtainDeviceCode(): MicrosoftDeviceCode {
        val response = mapOf(
            "client_id" to CLIENT_ID,
            "scope" to "XboxLive.signin offline_access",
        ).postData(DEVICE_CODE_URL)

        if (response.statusCode != 200) {
            throw MicrosoftAPIException(response)
        }

        return Jackson.MAPPER.convertValue(response.body, MicrosoftDeviceCode::class.java)
    }

    fun checkDeviceCode(deviceCode: MicrosoftDeviceCode): AuthenticationResponse? {
        val response = mapOf(
            "grant_type" to "urn:ietf:params:oauth:grant-type:device_code",
            "client_id" to CLIENT_ID,
            "device_code" to deviceCode.deviceCode,
        ).postData(TOKEN_CHECK_URL)

        if (response.statusCode != 200) {
            val error = MicrosoftAPIException(response)
            if (error.error?.error == "authorization_pending") {
                return null
            }
            throw error
        }

        return Jackson.MAPPER.convertValue(response.body, AuthenticationResponse::class.java)
    }

    fun refreshToken(token: MicrosoftTokens): AuthenticationResponse {
        val response = mapOf(
            "client_id" to CLIENT_ID,
            "grant_type" to "refresh_token",
            "scope" to "XboxLive.signin offline_access",
            "refresh_token" to token.refreshToken,
        ).postData(TOKEN_CHECK_URL)

        if (response.statusCode != 200) {
            throw MicrosoftAPIException(response)
        }

        return Jackson.MAPPER.convertValue(response.body, AuthenticationResponse::class.java)
    }

    fun loginToMicrosoftAccount(response: AuthenticationResponse, latch: CountUpAndDownLatch? = null): MicrosoftAccount {
        Log.log(LogMessageType.AUTHENTICATION, LogLevels.INFO) { "Logging into microsoft account..." }
        latch?.let { it.count += 6 }
        val msaTokens = response.saveTokens()
        val xboxLiveToken = getXboxLiveToken(msaTokens)
        latch?.dec()
        val xstsToken = getXSTSToken(xboxLiveToken)
        latch?.dec()

        val minecraftToken = getMinecraftBearerAccessToken(xboxLiveToken, xstsToken).saveTokens()
        latch?.dec()
        val profile = AccountUtil.fetchMinecraftProfile(minecraftToken)
        latch?.dec()

        val playerProperties = PlayerProperties.fetch(profile.uuid)
        latch?.dec()

        val account = MicrosoftAccount(
            uuid = profile.uuid,
            username = profile.name,
            msa = msaTokens,
            minecraft = minecraftToken,
            properties = playerProperties,
        )
        account.state = AccountStates.WORKING

        Log.log(LogMessageType.AUTHENTICATION, LogLevels.INFO) { "Microsoft account login successful (username=${account.username}, uuid=${account.uuid})" }
        latch?.dec()

        return account
    }

    fun getXboxLiveToken(msaTokens: MicrosoftTokens): XboxLiveToken {
        val response = mapOf(
            "Properties" to mapOf(
                "AuthMethod" to "RPS",
                "SiteName" to "user.auth.xboxlive.com",
                "RpsTicket" to "d=${msaTokens.accessToken}",
            ),
            "RelyingParty" to "http://auth.xboxlive.com",
            "TokenType" to "JWT",
        ).postJson(XBOX_LIVE_AUTH_URL)


        if (response.statusCode != 200 || response.body == null) {
            throw XboxAPIException(response)
        }

        return Jackson.MAPPER.convertValue(response.body, XboxLiveToken::class.java)
    }

    fun getXSTSToken(xBoxLiveToken: XboxLiveToken): XSTSToken {
        val response = mapOf(
            "Properties" to mapOf(
                "SandboxId" to "RETAIL",
                "UserTokens" to listOf(xBoxLiveToken.token)
            ),
            "RelyingParty" to "rp://api.minecraftservices.com/",
            "TokenType" to "JWT",
        ).postJson(XSTS_URL)

        if (response.statusCode != 200) {
            val error = Jackson.MAPPER.convertValue(response.body, XboxAPIError::class.java)
            val errorMessage = when (error.error) {
                2148916233 -> "You don't have an XBox account!"
                2148916235 -> "Xbox Live is banned in your country!"
                2148916236, 2148916237 -> "Your account needs adult verification (South Korea)"
                2148916238 -> "This account is a child account!"
                else -> error.message ?: "Unknown"
            }
            throw XboxAPIException(response.statusCode, error, errorMessage)
        }

        return Jackson.MAPPER.convertValue(response.body, XSTSToken::class.java)
    }

    fun getMinecraftBearerAccessToken(xBoxLiveToken: XboxLiveToken, xstsToken: XSTSToken): MinecraftBearerResponse {
        val response = mapOf(
            "identityToken" to "XBL3.0 x=${xBoxLiveToken.userHash};${xstsToken.token}",
            "ensureLegacyEnabled" to true,
        ).postJson(LOGIN_WITH_XBOX_URL)

        if (response.statusCode != 200) {
            throw MinecraftAPIException(response)
        }
        return Jackson.MAPPER.convertValue(response.body, MinecraftBearerResponse::class.java)
    }
}
