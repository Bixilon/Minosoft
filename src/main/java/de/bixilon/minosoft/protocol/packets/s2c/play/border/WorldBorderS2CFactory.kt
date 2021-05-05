/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *  
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *  
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *  
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.protocol.packets.s2c.play.border

import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum

object WorldBorderS2CFactory {

    // this function was signed by Maximilian Rosenmüller
    // and that means A LOT!
    fun createPacket(buffer: PlayInByteBuffer): PlayS2CPacket {
        return when (WorldBorderActions[buffer.readVarInt()]) {
            WorldBorderActions.SET_SIZE -> SetSizeWorldBorderS2CPacket(buffer)
            WorldBorderActions.LERP_SIZE -> LerpSizeWorldBorderS2CPacket(buffer)
            WorldBorderActions.SET_CENTER -> SetCenterWorldBorderS2CPacket(buffer)
            WorldBorderActions.INITIALIZE -> InitializeWorldBorderS2CPacket(buffer)
            WorldBorderActions.SET_WARNING_TIME -> SetWarningTimeWorldBorderS2CPacket(buffer)
            WorldBorderActions.SET_WARNING_BLOCKS -> SetWarningBlocksWorldBorderS2CPacket(buffer)
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
            override val NAME_MAP: Map<String, WorldBorderActions> = KUtil.getEnumValues(VALUES)
        }
    }
}
