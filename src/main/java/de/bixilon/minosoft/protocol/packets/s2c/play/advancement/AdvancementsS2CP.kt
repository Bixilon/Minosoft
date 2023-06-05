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

package de.bixilon.minosoft.protocol.packets.s2c.play.advancement

import de.bixilon.kutil.bit.BitByte.isBit
import de.bixilon.minosoft.advancements.Advancement
import de.bixilon.minosoft.advancements.AdvancementDisplay
import de.bixilon.minosoft.advancements.AdvancementFrames
import de.bixilon.minosoft.advancements.AdvancementProgress
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_23W18A
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket
class AdvancementsS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val reset = buffer.readBoolean()
    val advancements: Map<ResourceLocation, Advancement>
    val remove: Set<ResourceLocation>
    val progress: Map<ResourceLocation, Set<AdvancementProgress>>

    init {
        this.advancements = buffer.readMap(key = { buffer.readResourceLocation() }, value = { buffer.readAdvancement() })
        this.remove = buffer.readSet { buffer.readResourceLocation() }

        this.progress = buffer.readMap(key = { buffer.readResourceLocation() }, value = { buffer.readSet { buffer.readProgress() } })
    }

    override fun log(reducedLog: Boolean) {
        if (reducedLog) {
            Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Advancements (reset=$reset, advancements=${advancements.size}, progress=${progress.size})" }
        } else {
            Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Advancements (reset=$reset, advancements=$advancements, progress=$progress)" }
        }
    }

    fun PlayInByteBuffer.readProgress(): AdvancementProgress {
        val criterion = readString()
        val archiveTime = readOptional { readLong() }

        return AdvancementProgress(
            criterion = criterion,
            archiveTime = archiveTime,
        )
    }

    fun PlayInByteBuffer.readAdvancement(): Advancement {
        val parent = readOptional { readResourceLocation() }
        val display = readOptional { readDisplay() }
        val criteria = readSet { readString() }
        val requirements = readSet { readSet { readString() } }
        if (versionId >= V_23W18A) { // TODO: not 100% sure
            val sendTelemetry = readBoolean()
        }

        return Advancement(
            parent = parent,
            display = display,
            criteria = criteria,
            requirements = requirements,
        )
    }

    fun PlayInByteBuffer.readDisplay(): AdvancementDisplay {
        val title = readChatComponent()
        val description = readChatComponent()
        val icon = readItemStack()
        val frame = AdvancementFrames[readVarInt()]
        val flags = readInt()
        val background = if (flags.isBit(0)) readResourceLocation() else null
        val position = readVec2f()

        return AdvancementDisplay(
            title = title,
            description = description,
            icon = icon,
            frame = frame,
            background = background,
            position = position,
        )
    }
}
