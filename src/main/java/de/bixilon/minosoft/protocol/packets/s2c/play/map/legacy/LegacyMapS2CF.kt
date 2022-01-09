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

package de.bixilon.minosoft.protocol.packets.s2c.play.map.legacy

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.factory.PacketDirection
import de.bixilon.minosoft.protocol.packets.factory.factories.PlayPacketFactory
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer

@LoadPacket
object LegacyMapS2CF : PlayPacketFactory {
    override val direction = PacketDirection.SERVER_TO_CLIENT

    override fun createPacket(buffer: PlayInByteBuffer): LegacyMapS2CP {
        val id = buffer.readVarInt()
        val length = buffer.readUnsignedShort()
        val action = Actions[buffer.readUnsignedByte()]
        val data = PlayInByteBuffer(buffer.readByteArray(length - 1), buffer.connection) // 1 byte for the action
        return when (action) {
            Actions.DATA -> DataLegacyMapS2CP(id, data)
            Actions.PINS -> PinsLegacyMapS2CP(id, data)
            Actions.SCALE -> ScaleLegacyMapS2CP(id, data)
        }
    }

    private enum class Actions {
        DATA,
        PINS,
        SCALE,
        ;

        companion object : ValuesEnum<Actions> {
            override val VALUES: Array<Actions> = values()
            override val NAME_MAP: Map<String, Actions> = EnumUtil.getEnumValues(VALUES)
        }
    }
}
