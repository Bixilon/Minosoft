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

package de.bixilon.minosoft.data.accounts.types.microsoft

import com.fasterxml.jackson.annotation.JsonProperty
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.time.TimeUtil
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.accounts.AccountStates
import de.bixilon.minosoft.data.player.properties.PlayerProperties
import de.bixilon.minosoft.data.registries.CompanionResourceLocation
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.account.AccountUtil
import de.bixilon.minosoft.util.account.microsoft.MicrosoftOAuthUtils
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import org.jetbrains.annotations.Nullable
import java.net.ConnectException
import java.util.*

class MicrosoftAccount(
    override val uuid: UUID,
    username: String,
    @field:JsonProperty private var msa: MicrosoftTokens,
    @field:JsonProperty private var minecraft: MinecraftTokens,
    override val properties: PlayerProperties?,
) : Account(username) {
    override val id: String = uuid.toString()
    override val type: ResourceLocation = RESOURCE_LOCATION

    @Synchronized
    override fun join(serverId: String) {
        tryCheck(null, "null")
        AccountUtil.joinMojangServer(username, minecraft.accessToken, uuid, serverId)
    }

    override fun logout(clientToken: String) = Unit

    @Synchronized
    override fun check(latch: CountUpAndDownLatch?, @Nullable clientToken: String) {
        val innerLatch = CountUpAndDownLatch(1, latch)
        try {
            this.error = null
            checkMinecraftToken(innerLatch)
            innerLatch.dec()
            state = AccountStates.WORKING
        } catch (exception: ConnectException) {
            innerLatch.count = 0
            Log.log(LogMessageType.AUTHENTICATION, LogLevels.INFO) { "Could not check account ($this), we are probably offline" }
            Log.log(LogMessageType.AUTHENTICATION, LogLevels.VERBOSE) { exception }
            this.state = AccountStates.OFFLINE
        } catch (exception: Throwable) {
            innerLatch.count = 0
            this.error = exception
            this.state = AccountStates.ERRORED
            Log.log(LogMessageType.AUTHENTICATION, LogLevels.VERBOSE) { exception }
            throw exception
        }
    }

    override fun tryCheck(latch: CountUpAndDownLatch?, clientToken: String) {
        if (state == AccountStates.CHECKING || state == AccountStates.REFRESHING) {
            // already checking
            return
        }
        if (minecraft.expires >= TimeUtil.millis / 1000) {
            return check(latch, "null")
        }
        if (state == AccountStates.WORKING) {
            // Nothing to do
            return
        }
        check(latch, clientToken)
    }

    private fun refreshMicrosoftToken(latch: CountUpAndDownLatch?) {
        state = AccountStates.REFRESHING
        latch?.inc()
        msa = MicrosoftOAuthUtils.refreshToken(msa).saveTokens()
        latch?.dec()
    }

    private fun refreshMinecraftToken(latch: CountUpAndDownLatch?) {
        state = AccountStates.REFRESHING
        val time = TimeUtil.millis / 1000
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

    private fun checkMinecraftToken(latch: CountUpAndDownLatch?) {
        state = AccountStates.CHECKING
        val time = TimeUtil.millis / 1000
        if (time >= minecraft.expires) {
            // token expired
            refreshMinecraftToken(latch)
        }

        try {
            latch?.inc()
            AccountUtil.fetchMinecraftProfile(minecraft)
            latch?.dec()
            state = AccountStates.WORKING
        } catch (exception: Throwable) {
            refreshMinecraftToken(latch)
        }
    }

    override fun toString(): String {
        return "MicrosoftAccount{$username}"
    }

    companion object : CompanionResourceLocation {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:microsoft_account".toResourceLocation()
    }
}
