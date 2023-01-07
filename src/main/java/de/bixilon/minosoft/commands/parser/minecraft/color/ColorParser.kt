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

package de.bixilon.minosoft.commands.parser.minecraft.color

import de.bixilon.minosoft.commands.parser.ArgumentParser
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.suggestion.ArraySuggestion
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.commands.util.ReadResult
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asColor
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class ColorParser(
    val supportsRGB: Boolean = true,
) : ArgumentParser<RGBColor> {
    override val examples: List<Any> = listOf("red", "yellow")
    private val suggestions = ArraySuggestion(ChatColors.NAME_MAP.keys, true)

    override fun parse(reader: CommandReader): RGBColor {
        reader.readResult { reader.readColor() }.let { return it.result ?: throw ColorParseError(reader, it) }
    }

    fun CommandReader.readColor(): RGBColor? {
        val peek = peek() ?: return null
        if (peek == '#'.code) {
            if (!supportsRGB) {
                throw HexNotSupportedError(this, readResult { read()!!.toChar() })
            }
            read()
            val colorString = readWord(false) ?: return null
            return try {
                colorString.asColor()
            } catch (ignored: NumberFormatException) {
                null
            }
        }
        val string = readString(false) ?: return null
        if (string == "reset") {
            return ChatColors.WHITE // ToDo
        }
        return ChatColors.NAME_MAP[string.lowercase()]
    }

    override fun getSuggestions(reader: CommandReader): Collection<Any> {
        if (reader.peek() == '#'.code) {
            reader.read()
            if (!supportsRGB) {
                throw HexNotSupportedError(reader, reader.readResult { reader.read()!!.toChar() })
            }
            val pointer = reader.pointer
            val hex = reader.readWord(false) ?: return emptyList()
            try {
                hex.asColor()
            } catch (exception: NumberFormatException) {
                throw ColorParseError(reader, ReadResult(pointer, reader.pointer, hex, null))
            }
            return emptyList()
        }
        val pointer = reader.pointer
        val string = reader.readWord()
        return suggestions.suggest(string) ?: throw ColorParseError(reader, ReadResult(pointer, reader.pointer, string ?: "", null))
    }


    companion object : ArgumentParserFactory<ColorParser> {
        override val identifier: ResourceLocation = "minecraft:color".toResourceLocation()

        override fun read(buffer: PlayInByteBuffer): ColorParser {
            return ColorParser(buffer.connection.version.supportsRGBChat)
        }
    }
}
