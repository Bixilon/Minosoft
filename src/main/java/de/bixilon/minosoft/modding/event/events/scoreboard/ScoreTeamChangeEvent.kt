package de.bixilon.minosoft.modding.event.events.scoreboard

import de.bixilon.minosoft.data.scoreboard.ScoreboardObjective
import de.bixilon.minosoft.data.scoreboard.ScoreboardScore
import de.bixilon.minosoft.data.scoreboard.Team
import de.bixilon.minosoft.modding.event.events.connection.play.PlayConnectionEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class ScoreTeamChangeEvent(
    connection: PlayConnection,
    val objective: ScoreboardObjective,
    val score: ScoreboardScore,
    val team: Team,
    val remove: Boolean,
) : PlayConnectionEvent(connection)
