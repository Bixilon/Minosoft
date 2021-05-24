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

package de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard.teams

import de.bixilon.minosoft.data.scoreboard.NameTagVisibilities
import de.bixilon.minosoft.data.scoreboard.Team
import de.bixilon.minosoft.data.scoreboard.TeamCollisionRules
import de.bixilon.minosoft.data.text.ChatCode
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.BitByte.isBitMask
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

class TeamCreateS2CP(val name: String, buffer: PlayInByteBuffer) : PlayS2CPacket() {
    val displayName = buffer.readChatComponent()
    lateinit var prefix: ChatComponent
        private set
    lateinit var suffix: ChatComponent
        private set
    var friendlyFire = false
        private set
    var canSeeInvisibleTeam = false
        private set
    var collisionRule = TeamCollisionRules.NEVER
        private set
    var nameTagVisibility = NameTagVisibilities.ALWAYS
        private set
    var formattingCode: ChatCode? = null
        private set
    val members: Set<String>


    init {
        if (buffer.versionId < ProtocolVersions.V_18W01A) {
            this.prefix = buffer.readChatComponent()
            this.suffix = buffer.readChatComponent()
        }

        if (buffer.versionId < ProtocolVersions.V_16W06A) { // ToDo
            setLegacyFriendlyFire(buffer.readUnsignedByte())
        } else {
            buffer.readUnsignedByte().let {
                this.friendlyFire = it.isBitMask(0x01)
                this.canSeeInvisibleTeam = it.isBitMask(0x02)
            }
        }

        if (buffer.versionId >= ProtocolVersions.V_14W07A) {
            this.nameTagVisibility = NameTagVisibilities[buffer.readString()]
            if (buffer.versionId >= ProtocolVersions.V_16W06A) { // ToDo
                this.collisionRule = TeamCollisionRules[buffer.readString()]
            }
            if (buffer.versionId < ProtocolVersions.V_18W01A) {
                this.formattingCode = ChatColors.getFormattingById(buffer.readByte().toInt())
            } else {
                this.formattingCode = ChatColors.getFormattingById(buffer.readVarInt())
            }
        }

        if (buffer.versionId >= ProtocolVersions.V_18W20A) {
            prefix = buffer.readChatComponent()
            suffix = buffer.readChatComponent()
        }

        members = buffer.readStringArray(
            if (buffer.versionId < ProtocolVersions.V_14W04A) {
                buffer.readUnsignedShort()
            } else {
                buffer.readVarInt()
            }).toSet()
    }

    private fun setLegacyFriendlyFire(data: Int) {
        when (data) {
            0 -> this.friendlyFire = false
            1 -> this.friendlyFire = true
            2 -> {
                this.friendlyFire = false
                this.canSeeInvisibleTeam = true
            }
        }
        // ToDo: seeFriendlyInvisibles for case 0 and 1
    }


    override fun handle(connection: PlayConnection) {
        connection.scoreboardManager.teams[name] = Team(
            name = name,
            displayName = displayName,
            prefix = prefix,
            suffix = suffix,
            friendlyFire = friendlyFire,
            canSeeInvisibleTeam = canSeeInvisibleTeam,
            collisionRule = collisionRule,
            nameTagVisibility = nameTagVisibility,
            formattingCode = formattingCode,
            members = members.toMutableSet(),
        )
    }

    override fun log() {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Team create (name=$name, prefix=$prefix, suffix=$suffix, friendlyFire=$friendlyFire, canSeeInvisibleTeam=$canSeeInvisibleTeam, collisionRule=$collisionRule, nameTagVisibility=$nameTagVisibility, formattingCode=$formattingCode§r, members=$members)" }
    }
}
