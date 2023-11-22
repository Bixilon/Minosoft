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

package de.bixilon.minosoft.data.accounts.types.microsoft

import com.fasterxml.jackson.annotation.JacksonInject
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import de.bixilon.kutil.concurrent.lock.simple.SimpleLock
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.latch.AbstractLatch
import de.bixilon.kutil.latch.AbstractLatch.Companion.child
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager
import de.bixilon.minosoft.config.profile.storage.ProfileStorage
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.accounts.AccountStates
import de.bixilon.minosoft.data.entities.entities.player.properties.PlayerProperties
import de.bixilon.minosoft.data.registries.identified.Identified
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.account.AccountUtil
import de.bixilon.minosoft.util.account.microsoft.MicrosoftOAuthUtils
import de.bixilon.minosoft.util.account.minecraft.MinecraftPrivateKey
import de.bixilon.minosoft.util.account.minecraft.MinecraftTokens
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.net.ConnectException
import java.util.*

class MicrosoftAccount(
    override val uuid: UUID,
    @JacksonInject storage: ProfileStorage?,
    username: String,
    @field:JsonProperty private var msa: MicrosoftTokens,
    @field:JsonProperty private var minecraft: MinecraftTokens,
    @field:JsonProperty var key: MinecraftPrivateKey? = null,
    override val properties: PlayerProperties?,
) : Account(username, storage) {
    override val id: String = uuid.toString()
    override val type: ResourceLocation = identifier

    @JsonIgnore
    private val keyLock = SimpleLock()

    @Synchronized
    override fun join(serverId: String) {
        tryCheck(null)
        AccountUtil.joinMojangServer(minecraft.accessToken, uuid, serverId)
    }

    override fun logout() = Unit

    @Synchronized
    override fun check(latch: AbstractLatch?) {
        val innerLatch = latch?.child(1)
        try {
            this.error = null
            checkMinecraftToken(innerLatch)
            innerLatch?.dec()
            state = AccountStates.WORKING
        } catch (exception: ConnectException) {
            innerLatch?.count = 0
            Log.log(LogMessageType.AUTHENTICATION, LogLevels.INFO) { "Could not check account ($this), we are probably offline" }
            Log.log(LogMessageType.AUTHENTICATION, LogLevels.VERBOSE) { exception }
            this.state = AccountStates.OFFLINE
        } catch (exception: Throwable) {
            innerLatch?.count = 0
            this.error = exception
            this.state = AccountStates.ERRORED
            Log.log(LogMessageType.AUTHENTICATION, LogLevels.VERBOSE) { exception }
            throw exception
        }
    }

    override fun tryCheck(latch: AbstractLatch?) {
        if (state == AccountStates.CHECKING || state == AccountStates.REFRESHING) {
            // already checking
            return
        }
        if (minecraft.expires >= millis() / 1000) {
            return check(latch)
        }
        if (state == AccountStates.WORKING) {
            // Nothing to do
            return
        }
        check(latch)
    }

    private fun refreshMicrosoftToken(latch: AbstractLatch?) {
        state = AccountStates.REFRESHING
        latch?.inc()
        msa = MicrosoftOAuthUtils.refreshToken(msa).saveTokens()
        latch?.dec()
    }

    private fun refreshMinecraftToken(latch: AbstractLatch?) {
        state = AccountStates.REFRESHING
        val time = millis() / 1000
        if (time >= msa.expires) {
            // token expired
            refreshMicrosoftToken(latch)
        }

        try {
            latch?.let { it.count += 3 }
            val xboxLiveToken = MicrosoftOAuthUtils.getXboxLiveToken(msa)
            latch?.dec()
            val xstsToken = MicrosoftOAuthUtils.getXSTSToken(xboxLiveToken)
            latch?.dec()

            minecraft = MicrosoftOAuthUtils.getMinecraftBearerAccessToken(xboxLiveToken, xstsToken).saveTokens()
            latch?.dec()
        } catch (exception: Throwable) {
            Log.log(LogMessageType.AUTHENTICATION, LogLevels.VERBOSE) { exception }
            refreshMicrosoftToken(latch)
        }
        save()
    }

    private fun checkMinecraftToken(latch: AbstractLatch?) {
        state = AccountStates.CHECKING
        val time = millis() / 1000
        if (time >= minecraft.expires) {
            // token expired
            refreshMinecraftToken(latch)
        }

        try {
            if (AccountProfileManager.selected.alwaysFetchProfile) {
                latch?.inc()
                AccountUtil.fetchMinecraftProfile(minecraft)
                latch?.dec()
            }
            state = AccountStates.WORKING
        } catch (exception: Throwable) {
            refreshMinecraftToken(latch)
        }
    }

    override fun fetchKey(latch: AbstractLatch?): MinecraftPrivateKey {
        var key = key
        if (key == null || key.shouldRefresh() || key.signatureV2 == null) {
            keyLock.lock()
            try {
                key = AccountUtil.fetchPrivateKey(minecraft)
                this.key = key
                save()
                Log.log(LogMessageType.AUTHENTICATION, LogLevels.INFO) { "Fetched private key for $this. Expires at ${key.expiresAt}" }
            } finally {
                keyLock.unlock()
            }
        }

        return key ?: Broken()
    }

    override fun toString(): String {
        return "MicrosoftAccount{$username}"
    }

    companion object : Identified {
        override val identifier: ResourceLocation = "minosoft:microsoft_account".toResourceLocation()
    }
}
