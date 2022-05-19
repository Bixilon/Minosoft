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

package de.bixilon.minosoft.commands.parser.minosoft.enums

import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.commands.errors.suggestion.NoSuggestionError
import de.bixilon.minosoft.commands.parser.ArgumentParser
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.suggestion.ArraySuggestion
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class EnumParser<E : Enum<*>>(
    val values: ValuesEnum<E>,
) : ArgumentParser<E> {
    override val examples: List<E> = values.VALUES.toList()
    private val suggestion = ArraySuggestion(examples)
    override val placeholder = ChatComponent.of("<enum>")

    override fun parse(reader: CommandReader): E {
        reader.readResult { reader.readEnum() }.let { return it.result ?: throw EnumParseError(reader, it) }
    }

    fun CommandReader.readEnum(): E? {
        return values.getOrNull(readWord()?.lowercase()) // ToDo: Allow ordinals
    }

    override fun getSuggestions(reader: CommandReader): List<E> {
        val text = reader.readResult { reader.readWord() }
        if (text.result == null) {
            return examples
        }
        return suggestion.suggest(text.result) ?: throw NoSuggestionError(reader, text)
    }

    companion object : ArgumentParserFactory<EnumParser<*>> {
        override val RESOURCE_LOCATION: ResourceLocation = "minosoft:enum".toResourceLocation()

        override fun build(connection: PlayConnection?) = TODO("Can not construct enum parser yet!")

        override fun read(buffer: PlayInByteBuffer) = TODO("Can not construct enum parser yet!")
    }
}
