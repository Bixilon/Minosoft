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
package de.bixilon.minosoft.data.scoreboard

import de.bixilon.kutil.collections.CollectionUtil.lockMapOf
import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.collections.map.LockMap
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.minosoft.modding.event.events.scoreboard.ScoreTeamChangeEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class ScoreboardManager(private val connection: PlayConnection) {
    val teams: LockMap<String, Team> = lockMapOf()
    val objectives: LockMap<String, ScoreboardObjective> = lockMapOf()

    val positions: MutableMap<ScoreboardPositions, ScoreboardObjective> = synchronizedMapOf()


    fun getTeam(member: String): Team? {
        this.teams.lock.acquire()
        for (team in this.teams.values) {
            if (member !in team.members) {
                continue
            }
            this.teams.lock.release()
            return team
        }
        this.teams.lock.release()
        return null
    }

    fun updateScoreTeams(team: Team, members: Set<String>, remove: Boolean = false, fireEvent: Boolean = true) {
        objectives.lock.acquire()
        for (objective in objectives.values) {
            objective.scores.lock.acquire()
            for (score in objective.scores.values) {
                if (score.entity in members) {
                    score.team = remove.decide(null, team)
                    if (!fireEvent) {
                        continue
                    }
                    connection.fireEvent(ScoreTeamChangeEvent(connection, objective, score, team, remove))
                }
            }
            objective.scores.lock.release()
        }
        objectives.lock.release()
    }
}
