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

import de.bixilon.minosoft.data.scoreboard.ScoreboardObjective
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.KUtil
import de.bixilon.minosoft.util.enum.ValuesEnum
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class CreateScoreboardObjectiveS2CP(val objective: String, private var _displayName: ChatComponent?, buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val displayName: ChatComponent
        get() = _displayName!!
    var unit: ObjectiveUnits = ObjectiveUnits.INTEGER
        private set

    init {
        if (buffer.versionId >= ProtocolVersions.V_14W04A) { // ToDo
            this._displayName = buffer.readChatComponent()
        }
        if (buffer.versionId >= ProtocolVersions.V_14W08A) {
            when {
                buffer.versionId >= ProtocolVersions.V_17W47A && buffer.versionId < ProtocolVersions.V_17W49A -> {
                }
                buffer.versionId < ProtocolVersions.V_17W49A -> {
                    unit = ObjectiveUnits[buffer.readString()]
                }
                else -> {
                    unit = ObjectiveUnits[buffer.readVarInt()]
                }
            }
        }
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Create scoreboard objective (objective=$objective, displayName=$displayName, unit=$unit)" }
    }

    override fun handle(connection: PlayConnection) {
        connection.scoreboardManager.objectives[objective] = ScoreboardObjective(objective, displayName, unit)
    }

    enum class ObjectiveUnits {
        INTEGER,
        HEARTS,
        ;

        companion object : ValuesEnum<ObjectiveUnits> {
            override val VALUES: Array<ObjectiveUnits> = values()
            override val NAME_MAP: Map<String, ObjectiveUnits> = KUtil.getEnumValues(VALUES)
        }
    }

}
