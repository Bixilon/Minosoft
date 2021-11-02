package de.bixilon.minosoft.modding.event.events.scoreboard

import de.bixilon.minosoft.data.scoreboard.ScoreboardObjective
import de.bixilon.minosoft.modding.event.events.connection.play.PlayConnectionEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class ScoreboardObjectiveCreateEvent(
    connection: PlayConnection,
    val objective: ScoreboardObjective,
) : PlayConnectionEvent(connection)
