package de.bixilon.minosoft.modding.event.events.scoreboard

import de.bixilon.minosoft.data.scoreboard.ScoreboardObjective
import de.bixilon.minosoft.data.scoreboard.ScoreboardPositions
import de.bixilon.minosoft.modding.event.events.connection.play.PlayConnectionEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class ObjectivePositionSetEvent(
    connection: PlayConnection,
    val position: ScoreboardPositions,
    val objective: ScoreboardObjective?,
) : PlayConnectionEvent(connection)
