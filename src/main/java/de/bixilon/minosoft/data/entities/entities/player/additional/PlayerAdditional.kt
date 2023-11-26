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

package de.bixilon.minosoft.data.entities.entities.player.additional

import de.bixilon.kutil.cast.CastUtil.unsafeNull
import de.bixilon.kutil.observer.DataObserver.Companion.observe
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.entities.entities.player.properties.PlayerProperties
import de.bixilon.minosoft.data.scoreboard.team.Team
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.PlayerPublicKey
import de.bixilon.minosoft.util.KUtil.nullCompare

class PlayerAdditional(
    name: String,
    ping: Int = -1,
    gamemode: Gamemodes = Gamemodes.SURVIVAL,
    displayName: ChatComponent? = null,
    properties: PlayerProperties? = null,
    team: Team? = null,
    publicKey: PlayerPublicKey? = null,
    listed: Boolean = true,
) : Comparable<PlayerAdditional> {
    var name by observed(name)
    var ping by observed(ping)
    var gamemode by observed(gamemode)
    var displayName by observed(displayName)
    var properties by observed(properties)
    var team by observed(team)
    var publicKey by observed(publicKey)
    var listed by observed(listed)

    var tabDisplayName: ChatComponent = unsafeNull()
        private set

    private fun updateTabName() {
        tabDisplayName = displayName ?: ChatComponent.of(name).let { team?.formatting?.decorate(it) ?: it }
    }

    init {
        this::name.observe(this) { updateTabName() }
        this::team.observe(this) { updateTabName() }
        this::displayName.observe(this) { updateTabName() }
        updateTabName()
    }

    fun merge(data: AdditionalDataUpdate) {
        spareMerge(data)
        data.gamemode?.let { gamemode = it }
        data.publicKey?.let { publicKey = it }
        data.properties?.let { properties = it }
    }

    fun spareMerge(data: AdditionalDataUpdate) {
        data.name?.let { name = it }
        data.ping?.let { ping = it }

        data.hasDisplayName?.let {
            displayName = if (it) data.displayName!! else null
        }

        if (data.removeFromTeam) {
            this.team = null
        }
        data.team?.let { team = it }
        data.listed?.let { listed = it }
    }

    override fun compareTo(other: PlayerAdditional): Int {
        if (this.gamemode != other.gamemode) {
            if (this.gamemode == Gamemodes.SPECTATOR) {
                return -1
            }
            if (other.gamemode == Gamemodes.SPECTATOR) {
                return 1
            }
        }

        this.team?.name?.nullCompare(other.team?.name)?.let { return it }

        this.name.lowercase().nullCompare(other.name.lowercase())?.let { return it }

        return 0
    }
}
