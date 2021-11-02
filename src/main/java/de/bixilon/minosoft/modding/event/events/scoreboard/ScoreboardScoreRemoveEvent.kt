package de.bixilon.minosoft.modding.event.events.scoreboard

import de.bixilon.minosoft.data.scoreboard.ScoreboardScore
import de.bixilon.minosoft.modding.event.events.connection.play.PlayConnectionEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class ScoreboardScoreRemoveEvent(
    connection: PlayConnection,
    val score: ScoreboardScore,
) : PlayConnectionEvent(connection)
