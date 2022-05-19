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

package de.bixilon.minosoft.commands.parser.brigadier._double

import de.bixilon.minosoft.commands.parser.brigadier.BrigadierParser
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.BitByte.isBitMask
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class DoubleParser(
    val min: Double = -Double.MAX_VALUE,
    val max: Double = Double.MAX_VALUE,
) : BrigadierParser<Double> {
    override val examples: List<Double> = listOf(1.0, -1.0, 1000.0)
    override val placeholder = ChatComponent.of("<double>")

    override fun parse(reader: CommandReader): Double {
        val result = reader.readResult { reader.readDouble() }
        val double = result.result ?: throw DoubleParseError(reader, result)
        if (double !in min..max) {
            throw DoubleOutOfRangeError(reader, result, min, max)
        }

        return double
    }

    override fun getSuggestions(reader: CommandReader): List<Double> {
        if (reader.readString()?.isBlank() != false) {
            return examples
        }
        return emptyList()
    }

    companion object : ArgumentParserFactory<DoubleParser> {
        override val RESOURCE_LOCATION: ResourceLocation = "brigadier:double".toResourceLocation()

        override fun build(connection: PlayConnection?) = DoubleParser()

        override fun read(buffer: PlayInByteBuffer): DoubleParser {
            val flags = buffer.readUnsignedByte()
            val min = if (flags.isBitMask(0x01)) buffer.readDouble() else -Double.MAX_VALUE
            val max = if (flags.isBitMask(0x03)) buffer.readDouble() else Double.MAX_VALUE
            return DoubleParser(min = min, max = max)
        }

        fun CommandReader.readDouble(): Double? {
            return readNumeric()?.toDoubleOrNull()
        }
    }
}
