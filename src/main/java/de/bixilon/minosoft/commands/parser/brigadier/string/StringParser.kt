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

package de.bixilon.minosoft.commands.parser.brigadier.string

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.commands.parser.brigadier.BrigadierParser
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class StringParser(
    val mode: StringModes = StringModes.SINGLE,
) : BrigadierParser<String> {
    override val examples: List<String> = emptyList()
    override val placeholder = ChatComponent.of("<string>")

    override fun parse(reader: CommandReader): String {
        reader.readResult { reader.readString(mode) }.let { return it.result ?: throw StringParseError(reader, it) }
    }

    override fun getSuggestions(reader: CommandReader): List<String> {
        reader.readString(mode)
        return examples
    }

    enum class StringModes {
        SINGLE,
        QUOTED,
        GREEDY,
        ;

        companion object : ValuesEnum<StringModes> {
            override val VALUES: Array<StringModes> = values()
            override val NAME_MAP: Map<String, StringModes> = EnumUtil.getEnumValues(VALUES)
        }
    }

    companion object : ArgumentParserFactory<StringParser> {
        override val RESOURCE_LOCATION: ResourceLocation = "brigadier:string".toResourceLocation()

        override fun read(buffer: PlayInByteBuffer): StringParser {
            return StringParser(StringModes[buffer.readVarInt()])
        }


        fun CommandReader.readString(mode: StringModes): String? {
            return when (mode) {
                StringModes.SINGLE -> readUnquotedString()
                StringModes.QUOTED -> readString()
                StringModes.GREEDY -> readRest()
            }
        }
    }
}
