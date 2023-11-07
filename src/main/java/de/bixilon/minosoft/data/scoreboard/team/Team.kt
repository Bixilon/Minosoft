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
package de.bixilon.minosoft.data.scoreboard.team

import de.bixilon.minosoft.data.scoreboard.TeamCollisionRules
import de.bixilon.minosoft.data.text.TextComponent

data class Team(
    val name: String,
    val formatting: TeamFormatting = TeamFormatting(TextComponent(name)),
    var friendlyFire: Boolean = true,
    val visibility: TeamVisibility = TeamVisibility(),
    var collisions: TeamCollisionRules = TeamCollisionRules.ALWAYS,
    val members: MutableSet<String> = mutableSetOf(),
) {
    override fun toString(): String {
        return name
    }

    fun canSee(other: Team?): Boolean {
        // TODO
        if (other == null) return false
        if (other.name == this.name && visibility.invisibleTeam) return true


        return false
    }
}
