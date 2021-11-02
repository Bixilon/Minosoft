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

import de.bixilon.minosoft.modding.event.events.scoreboard.ScoreTeamChangeEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.toSynchronizedMap

class ScoreboardManager(private val connection: PlayConnection) {
    val teams: MutableMap<String, Team> = synchronizedMapOf()
    val objectives: MutableMap<String, ScoreboardObjective> = synchronizedMapOf()

    val positions: MutableMap<ScoreboardPositions, ScoreboardObjective> = synchronizedMapOf()


    fun getTeamsOf(member: String): Set<Team> {
        val teams: MutableSet<Team> = mutableSetOf()

        for ((_, team) in this.teams) {
            if (!team.members.contains(member)) {
                continue
            }
            teams += team
        }

        return teams
    }

    fun updateScoreTeams(team: Team, members: Set<String>, remove: Boolean = false, fireEvent: Boolean = true) {
        for ((_, objective) in objectives.toSynchronizedMap()) {
            for ((_, score) in objective.scores.toSynchronizedMap()) {
                if (score.entity in members) {
                    if (remove) {
                        score.teams -= team
                    } else {
                        score.teams += team
                    }
                    if (!fireEvent) {
                        continue
                    }
                    connection.fireEvent(ScoreTeamChangeEvent(connection, objective, score, team, remove))
                }
            }
        }
    }
}
