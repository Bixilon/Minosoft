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

package de.bixilon.minosoft.commands.parser.brigadier._float

import de.bixilon.minosoft.commands.parser.brigadier.BrigadierParser
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.BitByte.isBitMask
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class FloatParser(
    val min: Float = -Float.MAX_VALUE,
    val max: Float = Float.MAX_VALUE,
) : BrigadierParser<Float> {
    override val examples: List<Float> = listOf(1.0f, -1.0f, 1000.0f)
    override val placeholder = ChatComponent.of("<float>")

    override fun parse(reader: CommandReader): Float {
        val result = reader.readResult { reader.readFloat() }
        val float = result.result ?: throw FloatParseError(reader, result)
        if (float !in min..max) {
            throw FloatOutOfRangeError(reader, result, min, max)
        }

        return float
    }

    override fun getSuggestions(reader: CommandReader): List<Float> {
        if (reader.readString()?.isBlank() != false) {
            return examples
        }
        return listOf()
    }

    companion object : ArgumentParserFactory<FloatParser> {
        override val RESOURCE_LOCATION: ResourceLocation = "brigadier:float".toResourceLocation()

        override fun build(connection: PlayConnection?) = FloatParser()

        override fun read(buffer: PlayInByteBuffer): FloatParser {
            val flags = buffer.readUnsignedByte()
            val min = if (flags.isBitMask(0x01)) buffer.readFloat() else -Float.MAX_VALUE
            val max = if (flags.isBitMask(0x03)) buffer.readFloat() else Float.MAX_VALUE
            return FloatParser(min = min, max = max)
        }

        fun CommandReader.readFloat(): Float? {
            return readNumeric()?.toFloatOrNull()
        }
    }
}
