package de.bixilon.minosoft.modding.event.events.scoreboard.team

import de.bixilon.minosoft.data.scoreboard.Team
import de.bixilon.minosoft.modding.event.events.scoreboard.ScoreboardTeamMemberEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class TeamMemberAddEvent(
    connection: PlayConnection,
    team: Team,
    members: Set<String>,
) : ScoreboardTeamMemberEvent(connection, team, members)
