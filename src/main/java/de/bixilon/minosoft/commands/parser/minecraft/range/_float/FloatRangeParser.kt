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

package de.bixilon.minosoft.commands.parser.minecraft.range._float

import de.bixilon.minosoft.commands.parser.ArgumentParser
import de.bixilon.minosoft.commands.parser.brigadier._float.FloatParser.Companion.readFloat
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.parser.minecraft.range.RangeParserFactory.readRange
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class FloatRangeParser(
    val defaultMin: Float? = -Float.MAX_VALUE,
) : ArgumentParser<FloatRange> {
    override val examples: List<Any> = listOf(1.0f, "1.0..10")

    override fun parse(reader: CommandReader): FloatRange {
        return reader.readResult { reader.readFloatRange(defaultMin) }.let { return@let it.result ?: throw FloatRangeParseError(reader, it) }
    }

    override fun getSuggestions(reader: CommandReader): List<Any> {
        if (reader.readString()?.isBlank() != false) {
            return examples
        }
        return emptyList()
    }

    companion object : ArgumentParserFactory<FloatRangeParser> {
        override val identifier: ResourceLocation = "minecraft:float_range".toResourceLocation()

        override fun read(buffer: PlayInByteBuffer) = FloatRangeParser()

        fun CommandReader.readFloatRange(defaultMin: Float?): FloatRange? {
            val (first, second) = readRange { readFloat() } ?: return null
            if (first == null) {
                return null
            }
            if (second == null) {
                return FloatRange(min = defaultMin ?: first, max = first)
            }
            return FloatRange(min = first, max = second)
        }
    }
}
