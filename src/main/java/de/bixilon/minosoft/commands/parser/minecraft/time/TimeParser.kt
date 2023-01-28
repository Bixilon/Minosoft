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

package de.bixilon.minosoft.commands.parser.minecraft.time

import de.bixilon.minosoft.commands.parser.ArgumentParser
import de.bixilon.minosoft.commands.parser.brigadier._float.FloatParser
import de.bixilon.minosoft.commands.parser.brigadier._float.FloatParser.Companion.readFloat
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions.V_23W03A
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

// TODO: respect minimum
class TimeParser(val minimum: Int = 0) : ArgumentParser<Int> {
    override val examples: List<Any> = listOf(2400, "3d", "10s")

    override fun parse(reader: CommandReader): Int {
        reader.readResult { reader.readTime() }.let { return it.result }
    }

    fun CommandReader.readTime(): Int {
        val time = FloatParser.DEFAULT.parse(this)

        val unit: TimeUnit
        val peek = peekNext()
        try {
            unit = TimeUnit.fromUnit(peek)
            readNext()
        } catch (error: IllegalArgumentException) {
            throw InvalidTimeUnitError(this, this.pointer, peek)
        }
        return (time * unit.multiplier).toInt()
    }

    override fun getSuggestions(reader: CommandReader): List<Any> {
        reader.readFloat() ?: return examples
        val peek = reader.peekNext() ?: return TimeUnit.UNITS
        try {
            TimeUnit.fromUnit(peek)
        } catch (error: IllegalArgumentException) {
            throw InvalidTimeUnitError(reader, reader.pointer, peek)
        }
        return emptyList()
    }


    companion object : ArgumentParserFactory<TimeParser> {
        override val identifier: ResourceLocation = "minecraft:time".toResourceLocation()

        override fun read(buffer: PlayInByteBuffer): TimeParser {
            if (buffer.versionId < V_23W03A) {
                return TimeParser()
            }
            return TimeParser(buffer.readInt())
        }
    }
}
