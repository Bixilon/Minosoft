/*
 * Minosoft
 * Copyright (C) 2020-2024 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.entities.block.sign.SignSides
import de.bixilon.minosoft.modding.event.events.session.play.PlaySessionEvent
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import de.bixilon.minosoft.protocol.packets.s2c.play.sign.SignEditorS2CP

class OpenSignEditorEvent(
    session: PlaySession,
    val position: Vec3i,
    val side: SignSides,
) : PlaySessionEvent(session), CancelableEvent {

    constructor(session: PlaySession, packet: SignEditorS2CP) : this(session, packet.position, packet.side)
}
