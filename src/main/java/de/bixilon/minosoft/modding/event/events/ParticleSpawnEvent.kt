/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
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

import de.bixilon.minosoft.data.registries.particle.data.ParticleData
import de.bixilon.minosoft.modding.event.EventInitiators
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.play.ParticleS2CP
import glm_.vec3.Vec3
import glm_.vec3.Vec3d

class ParticleSpawnEvent(
    connection: PlayConnection,
    initiator: EventInitiators,
    position: Vec3d,
    offset: Vec3,
    val speed: Float,
    val count: Int,
    val data: ParticleData,
) : PlayConnectionEvent(connection, initiator), CancelableEvent {
    val position: Vec3d = position
        get() = Vec3d(field)
    val offset: Vec3 = offset
        get() = Vec3(field)

    constructor(connection: PlayConnection, packet: ParticleS2CP) : this(connection, EventInitiators.SERVER, packet.position, packet.offset, packet.speed, packet.count, packet.data)
}
