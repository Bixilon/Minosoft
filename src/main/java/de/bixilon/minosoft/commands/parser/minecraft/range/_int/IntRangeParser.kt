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

package de.bixilon.minosoft.commands.parser.minecraft.range._int

import de.bixilon.minosoft.commands.parser.ArgumentParser
import de.bixilon.minosoft.commands.parser.brigadier._int.IntParser.Companion.readInt
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.parser.minecraft.range.RangeParserFactory.readRange
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class IntRangeParser(
    val defaultMin: Boolean = true,
) : ArgumentParser<IntRange> {
    override val examples: List<Any> = listOf(1, "1..10")
    override val placeholder = ChatComponent.of("<int..int>")

    override fun parse(reader: CommandReader): IntRange {
        return reader.readResult { reader.readIntRange(defaultMin) }.let { return@let it.result ?: throw IntRangeParseError(reader, it) }
    }

    override fun getSuggestions(reader: CommandReader): List<Any> {
        if (reader.readString()?.isBlank() != false) {
            return examples
        }
        return emptyList()
    }

    companion object : ArgumentParserFactory<IntRangeParser> {
        override val RESOURCE_LOCATION: ResourceLocation = "minecraft:float_range".toResourceLocation()

        override fun read(buffer: PlayInByteBuffer) = IntRangeParser()

        fun CommandReader.readIntRange(defaultMin: Boolean = true): IntRange? {
            val (first, second) = readRange { readInt() } ?: return null
            if (first == null) {
                return null
            }
            if (second == null) {
                return if (defaultMin) {
                    Int.MIN_VALUE..first
                } else {
                    first..first
                }
            }
            return first..second
        }
    }
}
