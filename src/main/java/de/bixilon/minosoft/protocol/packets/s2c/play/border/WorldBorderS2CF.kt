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

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer

object WorldBorderS2CF {

    // this function was signed by Maximilian Rosenmüller
    // and that means A LOT!
    fun createPacket(buffer: PlayInByteBuffer): PlayS2CPacket {
        return when (WorldBorderActions[buffer.readVarInt()]) {
            WorldBorderActions.SET_SIZE -> SizeSetWorldBorderS2CPacket(buffer)
            WorldBorderActions.LERP_SIZE -> LerpSizeWorldBorderS2CPacket(buffer)
            WorldBorderActions.SET_CENTER -> CenterSetWorldBorderS2CPacket(buffer)
            WorldBorderActions.INITIALIZE -> InitializeWorldBorderS2CPacket(buffer)
            WorldBorderActions.SET_WARNING_TIME -> WarningTimeSetWorldBorderS2CPacket(buffer)
            WorldBorderActions.SET_WARNING_BLOCKS -> WarningBlocksSetWorldBorderS2CPacket(buffer)
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
            override val NAME_MAP: Map<String, WorldBorderActions> = EnumUtil.getEnumValues(VALUES)
        }
    }
}
