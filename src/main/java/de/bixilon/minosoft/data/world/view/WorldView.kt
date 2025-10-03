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

package de.bixilon.minosoft.data.world.view

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.protocol.network.session.play.PlaySession
import kotlin.math.abs

open class WorldView(
    private val session: PlaySession,
) {
    var serverViewDistance = Int.MAX_VALUE
        set(value) {
            if (field == value) {
                return
            }
            field = value
            viewDistance = minOf(value, session.profiles.block.viewDistance)
        }
    var viewDistance = session.profiles.block.viewDistance
        set(value) {
            val realValue = minOf(value, serverViewDistance)
            if (field == realValue) {
                return
            }
            field = realValue
            session.events.fire(ViewDistanceChangeEvent(session, realValue))
        }

    var serverSimulationDistance = Int.MAX_VALUE
        set(value) {
            field = value
            simulationDistance = minOf(value, session.profiles.block.simulationDistance)
        }
    var simulationDistance = session.profiles.block.simulationDistance
        set(value) {
            val realValue = minOf(value, serverSimulationDistance)
            if (field == realValue) {
                return
            }
            field = realValue
            session.events.fire(SimulationDistanceChangeEvent(session, realValue))
            particleViewDistance = minOf(realValue, session.profiles.particle.viewDistance)
        }

    var particleViewDistance = session.profiles.particle.viewDistance
        set(value) {
            val realValue = minOf(value, simulationDistance)
            if (field == realValue) {
                return
            }
            field = realValue
            session.events.fire(ParticleViewDistanceChangeEvent(session, realValue))
        }

    @Synchronized
    open fun updateServerDistance() {
        val cameraPosition = session.player.physics.positionInfo.chunkPosition
        val size = session.world.chunks.size.size
        val min = Vec2i(size.min.x - cameraPosition.x, size.min.y - cameraPosition.z)
        val max = Vec2i(size.max.x - cameraPosition.x, size.max.y - cameraPosition.z)
        serverViewDistance = maxOf(3, minOf(abs(min.x), abs(max.x), abs(min.y), abs(max.y)))
    }
}
