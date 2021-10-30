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

package de.bixilon.minosoft.protocol.packets.s2c.play.bossbar

import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum

object BossbarS2CPF {

    fun createPacket(buffer: PlayInByteBuffer): PlayS2CPacket {
        val uuid = buffer.readUUID()
        return when (BossBarActions[buffer.readVarInt()]) {
            BossBarActions.ADD -> BossbarAddS2CP(uuid, buffer)
            BossBarActions.REMOVE -> BossbarRemoveS2CP(uuid)
            BossBarActions.SET_VALUE -> BossbarValueSetS2CP(uuid, buffer)
            BossBarActions.SET_TITLE -> BossbarTitleSetS2CP(uuid, buffer)
            BossBarActions.SET_STYLE -> BossbarStyleSetS2CP(uuid, buffer)
            BossBarActions.SET_FLAGS -> BossbarFlagSetS2CP(uuid, buffer)
        }
    }

    enum class BossBarActions {
        ADD,
        REMOVE,
        SET_VALUE,
        SET_TITLE,
        SET_STYLE,
        SET_FLAGS,
        ;

        companion object : ValuesEnum<BossBarActions> {
            override val VALUES = values()
            override val NAME_MAP: Map<String, BossBarActions> = KUtil.getEnumValues(VALUES)
        }
    }
}
