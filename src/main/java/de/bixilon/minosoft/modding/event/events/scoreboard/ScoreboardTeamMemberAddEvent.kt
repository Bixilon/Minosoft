package de.bixilon.minosoft.modding.event.events.scoreboard

import de.bixilon.minosoft.data.scoreboard.Team
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class ScoreboardTeamMemberAddEvent(
    connection: PlayConnection,
    team: Team,
    members: Set<String>,
) : ScoreboardTeamMemberEvent(connection, team, members)
