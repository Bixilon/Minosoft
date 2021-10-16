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

package de.bixilon.minosoft.data.player.tab

import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.data.player.PlayerProperty
import de.bixilon.minosoft.data.scoreboard.Team
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.util.KUtil.nullCompare

data class TabListItem(
    var name: String,
    var ping: Int = -1,
    var gamemode: Gamemodes = Gamemodes.SURVIVAL,
    var displayName: ChatComponent = ChatComponent.of(name),
    var properties: Map<String, PlayerProperty> = mutableMapOf(),
    var team: Team? = null,
) : Comparable<TabListItem> {
    val tabDisplayName: ChatComponent
        get() {
            val displayName = BaseComponent()
            team?.prefix?.let {
                displayName += it
            }
            displayName += this.displayName.apply {
                // ToDo: Set correct formatting code
                val color = team?.formattingCode
                if (color is RGBColor) {
                    applyDefaultColor(color)
                }
            }
            team?.suffix?.let {
                displayName += it
            }
            return displayName
        }

    fun merge(data: TabListItemData) {
        specialMerge(data)
        data.gamemode?.let { gamemode = it }
    }

    fun specialMerge(data: TabListItemData) {
        data.name?.let { name = it }
        data.ping?.let { ping = it }

        data.hasDisplayName?.let {
            displayName = if (it) {
                data.displayName!!
            } else {
                ChatComponent.of(name)
            }
        }
        data.properties?.let { properties = it }

        if (data.removeFromTeam) {
            this.team = null
        }
        data.team?.let { team = it }
    }

    override fun compareTo(other: TabListItem): Int {
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
