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

package de.bixilon.minosoft.data.accounts.types.mojang

import com.fasterxml.jackson.annotation.JsonProperty
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.json.JsonUtil.asJsonObject
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.uuid.UUIDUtil.toUUID
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.accounts.AccountStates
import de.bixilon.minosoft.data.entities.entities.player.properties.PlayerProperties
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.account.AccountUtil
import de.bixilon.minosoft.util.http.HTTP2.postJson
import de.bixilon.minosoft.util.http.exceptions.AuthenticationException
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.net.ConnectException
import java.util.*

@Deprecated("Mojang authentication is legacy. Will be removed in the future!")
class MojangAccount(
    override val id: String,
    username: String,
    override val uuid: UUID,
    val email: String,
    @field:JsonProperty private var accessToken: String,
    override val properties: PlayerProperties?,
) : Account(username) {
    @Transient
    private var refreshed: Boolean = false
    override val type: ResourceLocation get() = identifier

    override fun join(serverId: String) {
        AccountUtil.joinMojangServer(username, accessToken, uuid, serverId)
    }

    override fun logout(clientToken: String) {
        val response = mutableMapOf(
            "accessToken" to accessToken,
            "clientToken" to clientToken,
        ).postJson(MOJANG_URL_INVALIDATE)


        if (response.statusCode != 200) {
            throw AuthenticationException(response.statusCode)
        }
        state = AccountStates.EXPIRED
        Log.log(LogMessageType.AUTHENTICATION, LogLevels.VERBOSE) { "Mojang account login successful (username=$username)" }
    }

    override fun check(latch: AbstractLatch?, clientToken: String) {
        if (refreshed) {
            return
        }
        try {
            latch?.inc()
            refresh(clientToken)
        } catch (exception: ConnectException) {
            exception.printStackTrace()
            state = AccountStates.OFFLINE
        } catch (exception: Throwable) {
            this.error = exception
            state = AccountStates.ERRORED
            throw exception
        }
        latch?.dec()
    }

    @Synchronized
    fun refresh(clientToken: String) {
        state = AccountStates.REFRESHING
        val response = mutableMapOf(
            "accessToken" to accessToken,
            "clientToken" to clientToken,
        ).postJson(MOJANG_URL_REFRESH)

        response.body!!

        if (response.statusCode != 200) {
            throw AuthenticationException(response.statusCode, response.body["errorMessage"].nullCast())
        }

        this.accessToken = response.body["accessToken"].unsafeCast()

        refreshed = true
        state = AccountStates.WORKING
        save()
        Log.log(LogMessageType.AUTHENTICATION, LogLevels.VERBOSE) { "Mojang account refresh successful (username=$username)" }
    }

    override fun toString(): String {
        return "MojangAccount{$username}"
    }

    companion object : Identified {
        private const val MOJANG_URL_LOGIN = "https://authserver.mojang.com/authenticate"
        private const val MOJANG_URL_REFRESH = "https://authserver.mojang.com/refresh"
        private const val MOJANG_URL_INVALIDATE = "https://authserver.mojang.com/invalidate"
        override val identifier: ResourceLocation = "minosoft:mojang_account".toResourceLocation()

        fun login(clientToken: String, email: String, password: String): MojangAccount {
            val response = mutableMapOf(
                "agent" to mutableMapOf(
                    "name" to "Minecraft",
                    "version" to 1,
                ),
                "username" to email,
                "password" to password,
                "clientToken" to clientToken,
                "requestUser" to true,
            ).postJson(MOJANG_URL_LOGIN)

            response.body!!

            if (response.statusCode != 200) {
                throw AuthenticationException(response.statusCode, response.body["errorMessage"]?.nullCast())
            }

            Log.log(LogMessageType.AUTHENTICATION, LogLevels.VERBOSE) { "Mojang login successful (email=$email)" }

            val uuid = response.body["selectedProfile"].asJsonObject()["id"].toString().toUUID()
            val account = MojangAccount(
                id = response.body["user"].asJsonObject()["id"].unsafeCast(),
                username = response.body["selectedProfile"].asJsonObject()["name"].unsafeCast(),
                uuid = uuid,
                email = email,
                accessToken = response.body["accessToken"].unsafeCast(),
                properties = PlayerProperties.fetch(uuid),
            )
            account.state = AccountStates.WORKING
            return account
        }
    }
}
