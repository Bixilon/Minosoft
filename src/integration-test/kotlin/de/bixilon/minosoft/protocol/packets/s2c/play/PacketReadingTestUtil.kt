/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.minosoft.protocol.network.connection.play.ConnectionTestUtil
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.S2CPacket
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import java.io.FileNotFoundException

object PacketReadingTestUtil {

    fun <T : S2CPacket> read(name: String, version: String, connection: PlayConnection = ConnectionTestUtil.createConnection(version = version), constructor: (PlayInByteBuffer) -> T): T {
        if (connection.version.name != version) throw IllegalStateException("Version mismatch: $version vs ${connection.version}")
        val data = PacketReadingTestUtil::class.java.getResourceAsStream("/packets/$name.bin")?.readAllBytes() ?: throw FileNotFoundException("Can not find packet blob $name")

        val buffer = PlayInByteBuffer(data, connection)

        val packet = constructor.invoke(buffer)
        if (buffer.pointer != buffer.size) throw IllegalArgumentException("buffer underflow!")

        return packet
    }
}
