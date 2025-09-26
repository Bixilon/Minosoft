/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
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

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.SoundCategories
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.modding.event.events.session.play.PlaySessionEvent
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.s2c.play.sound.NamedSoundS2CP
import de.bixilon.minosoft.protocol.packets.s2c.play.sound.SoundEventS2CP

class PlaySoundEvent(
    session: PlaySession,
    val category: SoundCategories?,
    val position: Vec3d,
    val soundEvent: ResourceLocation,
    val volume: Float,
    val pitch: Float,
) : PlaySessionEvent(session), CancelableEvent {

    constructor(session: PlaySession, packet: SoundEventS2CP) : this(session, packet.category, packet.position, packet.sound, packet.volume, packet.pitch)

    constructor(session: PlaySession, packet: NamedSoundS2CP) : this(session, packet.category, packet.position, packet.soundEvent!!, packet.volume, packet.pitch)
}
