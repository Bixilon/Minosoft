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
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.play.SoundEventS2CP
import glm_.vec3.Vec3i

class PlaySoundEvent(
    connection: PlayConnection,
    val category: SoundCategories?,
    position: Vec3i,
    val soundEvent: SoundEvent,
    val volume: Float,
    val pitch: Float,
) : CancelableEvent(connection) {
    val position: Vec3i = position
        get() = Vec3i(field)

    constructor(connection: PlayConnection, packet: SoundEventS2CP) : this(connection, packet.category, packet.position, packet.soundEvent, packet.volume, packet.pitch)
}
