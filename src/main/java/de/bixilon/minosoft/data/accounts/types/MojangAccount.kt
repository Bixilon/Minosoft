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
import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.accounts.AccountType
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.util.KUtil.asUUID
import de.bixilon.minosoft.util.KUtil.nullCast
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.account.AccountUtil
import de.bixilon.minosoft.util.http.HTTP2.postJson
import de.bixilon.minosoft.util.http.exceptions.AuthenticationException
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import de.bixilon.minosoft.util.nbt.tag.NBTUtil.asCompound
import java.util.*

class MojangAccount(
    override val id: String,
    username: String,
    val uuid: UUID,
    val email: String,
    @Json(name = "access_token") private var accessToken: String,
) : Account(username) {
    private var refreshed: Boolean = false
    override val type: ResourceLocation = RESOURCE_LOCATION

    override fun join(serverId: String) {
        AccountUtil.joinMojangServer(username, accessToken, uuid, serverId)
    }

    override fun logout() {
        val response = mutableMapOf(
            "accessToken" to accessToken,
            "clientToken" to Minosoft.config.config.account.clientToken,
        ).postJson(MOJANG_URL_INVALIDATE)


        if (response.statusCode != 200) {
            throw AuthenticationException(response.statusCode)
        }

        Log.log(LogMessageType.AUTHENTICATION, LogLevels.VERBOSE) { "Mojang account login successful (username=$username)" }
    }

    override fun verify() {
        if (refreshed) {
            return
        }
        refresh()
    }

    override fun serialize(): Map<String, Any> {
        return mapOf(
            "id" to id,
            "username" to username,
            "uuid" to uuid,
            "email" to email,
            "access_token" to accessToken,
            "type" to type,
        )
    }

    fun refresh() {
        val response = mutableMapOf(
            "accessToken" to accessToken,
            "clientToken" to Minosoft.config.config.account.clientToken,
        ).postJson(MOJANG_URL_REFRESH)

        response.body!!

        if (response.statusCode != 200) {
            throw AuthenticationException(response.statusCode, response.body["errorMessage"].nullCast())
        }

        this.accessToken = response.body["accessToken"].unsafeCast()

        refreshed = true
        Log.log(LogMessageType.AUTHENTICATION, LogLevels.VERBOSE) { "Mojang account refresh successful (username=$username)" }
    }

    override fun toString(): String {
        return "MojangAccount{$username}"
    }

    companion object : AccountType(MojangAccount::class) {
        private const val MOJANG_URL_LOGIN = "https://authserver.mojang.com/authenticate"
        private const val MOJANG_URL_REFRESH = "https://authserver.mojang.com/refresh"
        private const val MOJANG_URL_INVALIDATE = "https://authserver.mojang.com/invalidate"
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:mojang_account".toResourceLocation()

        fun login(clientToken: String = Minosoft.config.config.account.clientToken, email: String, password: String): MojangAccount {
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

            return MojangAccount(
                id = response.body["user"].asCompound()["id"].unsafeCast(),
                username = response.body["selectedProfile"].asCompound()["name"].unsafeCast(),
                uuid = response.body["selectedProfile"].asCompound()["id"].toString().asUUID(),
                email = email,
                accessToken = response.body["accessToken"].unsafeCast(),
            )
        }
    }
}
