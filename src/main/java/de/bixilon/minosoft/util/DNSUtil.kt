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
package de.bixilon.minosoft.util

import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import org.xbill.DNS.Lookup
import org.xbill.DNS.SRVRecord
import org.xbill.DNS.Type
import java.util.*

object DNSUtil {

    fun resolveServerAddress(hostname: String): List<ServerAddress> {
        val fallbackAddress = getServerAddress(hostname)
        val addresses: MutableList<ServerAddress> = mutableListOf()
        if (hostname.contains(":")) {
            // port provided, skip srv check
            addresses.add(fallbackAddress)
            return addresses
        }
        val query = "_minecraft._tcp.$hostname"
        val records = Lookup(query, Type.SRV).run()
        if (records == null) {
            addresses.add(fallbackAddress)
            return addresses
        }
        for (record in records) {
            check(record is SRVRecord)
            addresses.add(ServerAddress(record.target.toString(true), record.port))
        }
        addresses.add(fallbackAddress)
        return addresses
    }

    fun getServerAddress(hostname: String): ServerAddress {
        val splitHostname = fixAddress(hostname).split(":", limit = 2).toTypedArray()
        return if (splitHostname.size == 1) {
            ServerAddress(splitHostname[0], ProtocolDefinition.DEFAULT_PORT)
        } else {
            ServerAddress(splitHostname[0], splitHostname[1].toInt())
        }
    }

    fun fixAddress(hostname: String): String {
        // replaces invalid chars to avoid copy and paste issues (like spaces, ...)
        return hostname.replace("\\s+|((https|http):/{2})+|/".toRegex(), "").lowercase(Locale.ROOT)
    }
}
