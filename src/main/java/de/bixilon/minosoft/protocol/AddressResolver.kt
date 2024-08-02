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

package de.bixilon.minosoft.protocol

import de.bixilon.minosoft.protocol.address.ServerAddress
import de.bixilon.minosoft.protocol.network.NetworkConnection
import de.bixilon.minosoft.util.DNSUtil
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogMessageType

class AddressResolver(
    val address: String,
) {
    private var candidates: MutableList<ServerAddress> = DNSUtil.resolveServerAddress(address).toMutableList()


    fun tryNext(): NetworkConnection? {
        if (candidates.isEmpty()) return null
        val address = candidates.removeAt(0)

        Log.log(LogMessageType.NETWORK) { "Trying address: $address" }
        return NetworkConnection(address, false)
    }
}
