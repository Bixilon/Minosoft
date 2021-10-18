package de.bixilon.minosoft.data.registries.other.game.event.handlers.gamemode

import de.bixilon.minosoft.data.abilities.Gamemodes
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.modding.event.events.connection.play.PlayConnectionEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class GamemodeChangeEvent(
    connection: PlayConnection,
    initiator: EventInitiators,
    val previousGameMode: Gamemodes,
    val gamemode: Gamemodes,
) : PlayConnectionEvent(connection, initiator)
