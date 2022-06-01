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

package de.bixilon.minosoft.commands.parser.minecraft.color

import de.bixilon.minosoft.commands.parser.ArgumentParser
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatColors
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.text.RGBColor.Companion.asColor
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class ColorParser(
    val supportsRGB: Boolean = true,
) : ArgumentParser<RGBColor> {
    override val examples: List<Any> = listOf()
    override val placeholder = ChatComponent.of("<color>")

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
        val string = readString() ?: return null
        if (string == "reset") {
            return ChatColors.WHITE // ToDo
        }
        return ChatColors.NAME_MAP[string.lowercase()]
    }

    override fun getSuggestions(reader: CommandReader): List<Any> {
        if (reader.readString()?.isBlank() != false) {
            return examples
        }
        return emptyList()
    }


    companion object : ArgumentParserFactory<ColorParser> {
        override val RESOURCE_LOCATION: ResourceLocation = "minecraft:color".toResourceLocation()

        override fun read(buffer: PlayInByteBuffer): ColorParser {
            return ColorParser(buffer.connection.version.supportsRGBChat)
        }
    }
}
