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
package de.bixilon.minosoft.data.scoreboard

import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TextComponent
import de.bixilon.minosoft.data.text.formatting.color.RGBColor

data class Team(
    val name: String,
    var displayName: ChatComponent = TextComponent(name),
    var prefix: ChatComponent? = null,
    var suffix: ChatComponent? = null,
    var friendlyFire: Boolean = true,
    var canSeeInvisibleTeam: Boolean = true,
    var collisionRule: TeamCollisionRules = TeamCollisionRules.ALWAYS,
    var nameTagVisibility: NameTagVisibilities = NameTagVisibilities.ALWAYS,
    var color: RGBColor? = null,
    val members: MutableSet<String> = mutableSetOf(),
) {
    override fun toString(): String {
        return name
    }

    fun decorateName(name: ChatComponent): ChatComponent {
        val displayName = BaseComponent()
        prefix?.let { displayName += it }
        displayName += name.apply {
            color?.let { setFallbackColor(it) }
        }
        suffix?.let { displayName += it }
        return displayName
    }

    fun canSee(other: Team?): Boolean {
        if (other == null) return false
        if (other.name == this.name && canSeeInvisibleTeam) return true

        return false
    }
}
