package de.bixilon.minosoft.modding.event.events.scoreboard

import de.bixilon.minosoft.data.scoreboard.Team
import de.bixilon.minosoft.modding.event.events.connection.play.PlayConnectionEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

abstract class ScoreboardTeamMemberEvent(
    connection: PlayConnection,
    val team: Team,
    val members: Set<String>,
) : PlayConnectionEvent(connection)
