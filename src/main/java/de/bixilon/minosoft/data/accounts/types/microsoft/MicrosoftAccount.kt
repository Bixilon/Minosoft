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

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.minosoft.data.accounts.Account
import de.bixilon.minosoft.data.accounts.AccountStates
import de.bixilon.minosoft.data.player.properties.PlayerProperties
import de.bixilon.minosoft.data.registries.CompanionResourceLocation
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.account.AccountUtil
import de.bixilon.minosoft.util.account.microsoft.MicrosoftOAuthUtils
import org.jetbrains.annotations.Nullable
import java.util.*

class MicrosoftAccount(
    val uuid: UUID,
    username: String,
    @field:JsonProperty private val authorizationToken: String,
    override val properties: PlayerProperties?,
) : Account(username) {
    @Transient @JsonIgnore var accessToken: String? = null
    override val id: String = uuid.toString()
    override val type: ResourceLocation = RESOURCE_LOCATION

    @Synchronized
    override fun join(serverId: String) {
        AccountUtil.joinMojangServer(username, accessToken!!, uuid, serverId)
    }

    override fun logout(clientToken: String) = Unit

    @Synchronized
    override fun check(latch: CountUpAndDownLatch?, @Nullable clientToken: String) {
        if (accessToken != null) {
            return
        }
        val innerLatch = CountUpAndDownLatch(3, latch)
        try {
            state = AccountStates.REFRESHING
            val (xboxLiveToken, userHash) = MicrosoftOAuthUtils.getXboxLiveToken(authorizationToken)
            innerLatch.dec()
            val xstsToken = MicrosoftOAuthUtils.getXSTSToken(xboxLiveToken)
            innerLatch.dec()

            accessToken = MicrosoftOAuthUtils.getMinecraftBearerAccessToken(userHash, xstsToken)
            innerLatch.dec()
            state = AccountStates.WORKING
        } catch (exception: Throwable) {
            innerLatch.count = 0
            this.error = exception
            this.state = AccountStates.ERRORED
            throw exception
        }
    }

    override fun toString(): String {
        return "MicrosoftAccount{$username}"
    }

    companion object : CompanionResourceLocation {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:microsoft_account".toResourceLocation()
    }
}
