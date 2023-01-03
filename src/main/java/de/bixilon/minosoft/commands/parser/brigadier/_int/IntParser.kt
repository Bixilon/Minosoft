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

package de.bixilon.minosoft.commands.parser.brigadier._int

import de.bixilon.kutil.bit.BitByte.isBitMask
import de.bixilon.minosoft.commands.parser.brigadier.BrigadierParser
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.commands.util.StringReader
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class IntParser(
    val min: Int = Int.MIN_VALUE,
    val max: Int = Int.MAX_VALUE,
) : BrigadierParser<Int> {
    override val examples: List<Int> = listOf(1, -1, 1000)
    override val placeholder = ChatComponent.of("<int>")

    override fun parse(reader: CommandReader): Int {
        val result = reader.readResult { reader.readInt() }
        val int = result.result ?: throw IntParseError(reader, result)
        if (int !in min..max) {
            throw IntOutOfRangeError(reader, result, min, max)
        }

        return int
    }

    override fun getSuggestions(reader: CommandReader): List<Int> {
        if (reader.readString()?.isBlank() != false) {
            return examples
        }
        return emptyList()
    }

    companion object : ArgumentParserFactory<IntParser> {
        override val identifier: ResourceLocation = "brigadier:integer".toResourceLocation()
        val DEFAULT = IntParser()

        override fun read(buffer: PlayInByteBuffer): IntParser {
            val flags = buffer.readUnsignedByte()
            val min = if (flags.isBitMask(0x01)) buffer.readInt() else Int.MIN_VALUE
            val max = if (flags.isBitMask(0x03)) buffer.readInt() else Int.MAX_VALUE
            return IntParser(min = min, max = max)
        }

        fun StringReader.readInt(): Int? {
            return readNumeric(decimal = false)?.toIntOrNull()
        }

        fun StringReader.readRequiredInt(): Int {
            readResult { readInt() }.let { return it.result ?: throw IntParseError(this, it) }
        }
    }
}
