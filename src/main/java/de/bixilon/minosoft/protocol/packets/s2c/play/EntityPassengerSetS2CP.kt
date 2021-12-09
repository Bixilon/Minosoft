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
package de.bixilon.minosoft.protocol.packets.s2c.play

import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.entities
import de.bixilon.minosoft.util.KUtil.toSynchronizedSet
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class EntityPassengerSetS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val vehicleEntityId: Int = buffer.readVarInt()
    val passengerEntityIds: Set<Int> = buffer.readVarIntArray().toSet()

    override fun handle(connection: PlayConnection) {
        val vehicle = connection.world.entities[vehicleEntityId] ?: return
        val passengers = passengerEntityIds.entities(connection)

        for (passenger in vehicle.passengers) {
            passenger.vehicle = null
        }

        vehicle.passengers = passengers.toSynchronizedSet()

        for (passenger in passengers) {
            passenger.vehicle = vehicle
        }
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Entity passenger set (vehicleEntityId=$vehicleEntityId, passengerEntityIds=$passengerEntityIds)" }
    }
}
