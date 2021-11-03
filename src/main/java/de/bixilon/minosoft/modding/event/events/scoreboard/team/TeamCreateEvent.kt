package de.bixilon.minosoft.modding.event.events.scoreboard.team

import de.bixilon.minosoft.data.scoreboard.Team
import de.bixilon.minosoft.modding.event.events.connection.play.PlayConnectionEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class TeamCreateEvent(
    connection: PlayConnection,
    val team: Team,
) : PlayConnectionEvent(connection)
