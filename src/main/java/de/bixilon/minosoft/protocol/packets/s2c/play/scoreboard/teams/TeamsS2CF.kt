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

package de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard.teams

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.factory.PacketDirection
import de.bixilon.minosoft.protocol.packets.factory.factories.PlayPacketFactory
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer

@LoadPacket(threadSafe = false)
object TeamsS2CF : PlayPacketFactory {
    override val direction = PacketDirection.SERVER_TO_CLIENT

    override fun createPacket(buffer: PlayInByteBuffer): TeamsS2CP {
        val name = buffer.readString()
        return when (TeamActions[buffer.readUnsignedByte()]) {
            TeamActions.CREATE -> CreateTeamS2CP(name, buffer)
            TeamActions.REMOVE -> RemoveTeamS2CP(name)
            TeamActions.UPDATE -> UpdateTeamS2CP(name, buffer)
            TeamActions.MEMBER_ADD -> AddTeamMemberS2CP(name, buffer)
            TeamActions.MEMBER_REMOVE -> RemoveTeamMemberS2CP(name, buffer)
        }
    }

    enum class TeamActions {
        CREATE,
        REMOVE,
        UPDATE,
        MEMBER_ADD,
        MEMBER_REMOVE,
        ;

        companion object : ValuesEnum<TeamActions> {
            override val VALUES: Array<TeamActions> = values()
            override val NAME_MAP: Map<String, TeamActions> = EnumUtil.getEnumValues(VALUES)
        }
    }
}
