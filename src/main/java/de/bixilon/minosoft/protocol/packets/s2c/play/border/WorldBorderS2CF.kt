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

package de.bixilon.minosoft.protocol.packets.s2c.play.border

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.protocol.packets.registry.factory.PlayPacketFactory
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer

object WorldBorderS2CF : PlayPacketFactory {

    // this function was signed by Maximilian Rosenmüller
    // and that means A LOT!
    override fun create(buffer: PlayInByteBuffer): WorldBorderS2CP {
        return when (WorldBorderActions[buffer.readVarInt()]) {
            WorldBorderActions.SET_SIZE -> SizeWorldBorderS2CP(buffer)
            WorldBorderActions.INTERPOLATE_SIZE -> InterpolateWorldBorderS2CP(buffer)
            WorldBorderActions.SET_CENTER -> CenterWorldBorderS2CP(buffer)
            WorldBorderActions.INITIALIZE -> InitializeWorldBorderS2CP(buffer)
            WorldBorderActions.SET_WARNING_TIME -> WarnTimeWorldBorderS2CP(buffer)
            WorldBorderActions.SET_WARNING_BLOCKS -> WarnBlocksWorldBorderS2CP(buffer)
        }
    }

    enum class WorldBorderActions {
        SET_SIZE,
        INTERPOLATE_SIZE,
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
