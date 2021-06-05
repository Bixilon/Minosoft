/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.modding.event.events

import de.bixilon.minosoft.data.SoundCategories
import de.bixilon.minosoft.data.mappings.sounds.SoundEvent
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.play.NamedSoundEventS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.SoundEventS2CP
import glm_.vec3.Vec3

class PlaySoundEvent(
    connection: PlayConnection,
    initiator: EventInitiators,
    val category: SoundCategories?,
    position: Vec3,
    val soundEvent: SoundEvent,
    val volume: Float,
    val pitch: Float,
) : CancelableEvent(connection, initiator) {
    val position: Vec3 = position
        get() = Vec3(field)

    constructor(connection: PlayConnection, packet: SoundEventS2CP) : this(connection, EventInitiators.SERVER, packet.category, Vec3(packet.position), packet.soundEvent, packet.volume, packet.pitch)

    constructor(connection: PlayConnection, packet: NamedSoundEventS2CP) : this(connection, EventInitiators.SERVER, packet.category, packet.position, packet.soundEvent!!, packet.volume, packet.pitch)
}
