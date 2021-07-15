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

package de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard.objective

import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard.score.RemoveScoreboardScoreS2CP
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum

object ScoreboardObjectiveS2CF {

    fun createPacket(buffer: PlayInByteBuffer): PlayS2CPacket {
        val objective = buffer.readString()
        val displayName = if (buffer.versionId < ProtocolVersions.V_14W04A) { // ToDo
            buffer.readChatComponent()
        } else {
            null
        }
        return when (ScoreboardScoreActions[buffer.readUnsignedByte()]) {
            ScoreboardScoreActions.CREATE -> CreateScoreboardObjectiveS2CP(objective, displayName, buffer)
            ScoreboardScoreActions.REMOVE -> RemoveScoreboardScoreS2CP(objective, buffer)
            ScoreboardScoreActions.UPDATE -> UpdateScoreboardObjectiveS2CP(objective, displayName, buffer)
        }
    }

    enum class ScoreboardScoreActions {
        CREATE,
        REMOVE,
        UPDATE,
        ;

        companion object : ValuesEnum<ScoreboardScoreActions> {
            override val VALUES: Array<ScoreboardScoreActions> = values()
            override val NAME_MAP: Map<String, ScoreboardScoreActions> = KUtil.getEnumValues(VALUES)
        }
    }
}
