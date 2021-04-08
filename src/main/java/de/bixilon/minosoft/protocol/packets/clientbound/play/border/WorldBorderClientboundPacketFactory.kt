/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *  
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *  
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.clientbound.play.border

import de.bixilon.minosoft.protocol.packets.clientbound.PlayClientboundPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum

object WorldBorderClientboundPacketFactory {

    fun createPacket(buffer: PlayInByteBuffer): PlayClientboundPacket {
        return when (WorldBorderActions.VALUES[buffer.readVarInt()]) {
            WorldBorderActions.SET_SIZE -> SetSizeWorldBorderClientboundPacket(buffer)
            WorldBorderActions.LERP_SIZE -> LerpSizeWorldBorderClientboundPacket(buffer)
            WorldBorderActions.SET_CENTER -> SetCenterWorldBorderClientboundPacket(buffer)
            WorldBorderActions.INITIALIZE -> InitializeWorldBorderClientboundPacket(buffer)
            WorldBorderActions.SET_WARNING_TIME -> SetWarningTimeWorldBorderClientboundPacket(buffer)
            WorldBorderActions.SET_WARNING_BLOCKS -> SetWarningBlocksWorldBorderClientboundPacket(buffer)
        }
    }

    enum class WorldBorderActions {
        SET_SIZE,
        LERP_SIZE,
        SET_CENTER,
        INITIALIZE,
        SET_WARNING_TIME,
        SET_WARNING_BLOCKS,
        ;

        companion object : ValuesEnum<WorldBorderActions> {
            override val VALUES = values()
            override val NAME_MAP = KUtil.getEnumValues(VALUES)
        }
    }
}
