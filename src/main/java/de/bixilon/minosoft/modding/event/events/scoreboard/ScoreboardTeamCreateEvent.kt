package de.bixilon.minosoft.modding.event.events.scoreboard

import de.bixilon.minosoft.data.scoreboard.Team
import de.bixilon.minosoft.modding.event.events.connection.play.PlayConnectionEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class ScoreboardTeamCreateEvent(
    connection: PlayConnection,
    val team: Team,
) : PlayConnectionEvent(connection)
