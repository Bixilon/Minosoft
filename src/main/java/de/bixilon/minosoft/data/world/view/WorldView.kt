/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class WorldView(
    private val connection: PlayConnection,
) {
    var serverViewDistance = Int.MAX_VALUE
        set(value) {
            field = value
            viewDistance = minOf(value, connection.profiles.block.viewDistance)
        }
    var viewDistance = connection.profiles.block.viewDistance
        set(value) {
            val realValue = minOf(value, serverViewDistance)
            if (field == realValue) {
                return
            }
            field = realValue
            connection.fireEvent(ViewDistanceChangeEvent(connection, realValue))
        }

    var serverSimulationDistance = Int.MAX_VALUE
        set(value) {
            field = value
            simulationDistance = minOf(value, connection.profiles.block.simulationDistance)
        }
    var simulationDistance = connection.profiles.block.simulationDistance
        set(value) {
            val realValue = minOf(value, serverSimulationDistance)
            if (field == realValue) {
                return
            }
            field = realValue
            connection.fireEvent(SimulationDistanceChangeEvent(connection, realValue))
            particleViewDistance = minOf(realValue, connection.profiles.particle.viewDistance)
        }

    var particleViewDistance = connection.profiles.particle.viewDistance
        set(value) {
            val realValue = minOf(value, simulationDistance)
            if (field == realValue) {
                return
            }
            field = realValue
            connection.fireEvent(ParticleViewDistanceChangeEvent(connection, realValue))
        }

    fun updateServerDistance() {
        val size = connection.world.chunkMax - connection.world.chunkMin
        serverViewDistance = maxOf(3, maxOf(size.x, size.y) / 2 - 1)
    }
}
