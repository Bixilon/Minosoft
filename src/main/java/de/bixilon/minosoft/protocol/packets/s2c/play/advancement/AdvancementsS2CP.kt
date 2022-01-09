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

package de.bixilon.minosoft.protocol.packets.s2c.play.advancement

import de.bixilon.minosoft.advancements.Advancement
import de.bixilon.minosoft.advancements.AdvancementDisplay
import de.bixilon.minosoft.advancements.AdvancementFrames
import de.bixilon.minosoft.advancements.AdvancementProgress
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.BitByte.isBit
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket
class AdvancementsS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val reset = buffer.readBoolean()
    val advancements: Map<String, Advancement?>
    val progress: Map<String, Set<AdvancementProgress>>

    init {
        val advancements: MutableMap<String, Advancement?> = mutableMapOf()
        for (i in 0 until buffer.readVarInt()) {
            val key = buffer.readString()
            val parent = buffer.readOptional { readString() }
            val display = buffer.readPlayOptional { readDisplay() }
            val criteria = buffer.readStringArray().toSet()
            val requirements = buffer.readArray { buffer.readStringArray().toSet() }.toSet()

            advancements[key] = Advancement(
                parent = parent,
                display = display,
                criteria = criteria,
                requirements = requirements,
            )
        }
        for (remove in buffer.readStringArray()) {
            advancements[remove] = null
        }
        this.advancements = advancements

        val progress: MutableMap<String, Set<AdvancementProgress>> = mutableMapOf()
        for (i in 0 until buffer.readVarInt()) {
            val name = buffer.readString()
            val criteria: MutableSet<AdvancementProgress> = mutableSetOf()
            for (ii in 0 until buffer.readVarInt()) {
                val criterion = buffer.readString()
                val archiveTime = buffer.readOptional { readLong() }
                criteria += AdvancementProgress(
                    criterion = criterion,
                    archiveTime = archiveTime,
                )
            }
            progress[name] = criteria
        }
        this.progress = progress
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Advancements (reset=$reset, advancements=$advancements, progress=$progress)" }
    }

    fun PlayInByteBuffer.readDisplay(): AdvancementDisplay {
        val title = readChatComponent()
        val description = readChatComponent()
        val icon = readItemStack()
        val frame = AdvancementFrames[readVarInt()]
        val flags = readInt()
        val background = if (flags.isBit(0)) readString() else null
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
