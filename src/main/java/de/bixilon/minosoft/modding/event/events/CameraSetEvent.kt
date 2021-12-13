package de.bixilon.minosoft.modding.event.events

import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.modding.event.events.connection.play.PlayConnectionEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class CameraSetEvent(
    connection: PlayConnection,
    val entity: Entity,
) : PlayConnectionEvent(connection)
