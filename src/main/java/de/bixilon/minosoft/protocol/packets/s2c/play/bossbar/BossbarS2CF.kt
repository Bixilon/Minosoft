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

package de.bixilon.minosoft.protocol.packets.s2c.play.bossbar

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.protocol.packets.registry.factory.PlayPacketFactory
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer

object BossbarS2CF : PlayPacketFactory {

    override fun create(buffer: PlayInByteBuffer): BossbarS2CP {
        val uuid = buffer.readUUID()
        return when (BossbarActions[buffer.readVarInt()]) {
            BossbarActions.ADD -> AddBossbarS2CP(uuid, buffer)
            BossbarActions.REMOVE -> RemoveBossbarS2CP(uuid)
            BossbarActions.SET_VALUE -> ValueBossbarS2CP(uuid, buffer)
            BossbarActions.SET_TITLE -> TitleBossbarS2CP(uuid, buffer)
            BossbarActions.SET_STYLE -> StyleBossbarS2CP(uuid, buffer)
            BossbarActions.SET_FLAGS -> FlagsBossbarS2CP(uuid, buffer)
        }
    }

    enum class BossbarActions {
        ADD,
        REMOVE,
        SET_VALUE,
        SET_TITLE,
        SET_STYLE,
        SET_FLAGS,
        ;

        companion object : ValuesEnum<BossbarActions> {
            override val VALUES = values()
            override val NAME_MAP: Map<String, BossbarActions> = EnumUtil.getEnumValues(VALUES)
        }
    }
}
