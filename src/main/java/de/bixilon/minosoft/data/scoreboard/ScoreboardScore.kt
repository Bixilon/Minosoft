/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
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

class ScoreboardScore(
    val entity: String,
    var objective: ScoreboardObjective,
    var team: Team?,
    var value: Int,
) : Comparable<ScoreboardScore> {

    override fun toString(): String {
        return "$entity=$value"
    }

    override fun hashCode(): Int {
        return entity.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is ScoreboardScore) {
            return false
        }
        return entity == other.entity // ToDo: Compare all?
    }

    override fun compareTo(other: ScoreboardScore): Int {
        val difference = other.value - value
        if (difference != 0) {
            return difference
        }
        return entity.compareTo(other.entity) // ToDo
    }
}
