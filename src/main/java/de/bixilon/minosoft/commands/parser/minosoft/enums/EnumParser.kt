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

package de.bixilon.minosoft.commands.parser.minosoft.enums

import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.commands.parser.ArgumentParser
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.suggestion.Suggestion
import de.bixilon.minosoft.commands.suggestion.util.SuggestionUtil
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer

class EnumParser<E : Enum<*>>(
    val values: ValuesEnum<E>,
) : ArgumentParser<E> {
    override val examples: List<E> = values.VALUES.toList()

    override fun parse(reader: CommandReader): E {
        reader.readResult { reader.readEnum() }.let { return it.result ?: throw EnumParseError(reader, it) }
    }

    fun CommandReader.readEnum(): E? {
        return values.getOrNull(readWord()?.lowercase()) // ToDo: Allow ordinals
    }

    override fun getSuggestions(reader: CommandReader): Collection<Suggestion> {
        val text = reader.readResult { reader.readWord() }
        return SuggestionUtil.suggest(examples, text, false) ?: throw EnumParseError(reader, text)
    }

    companion object : ArgumentParserFactory<EnumParser<*>> {
        override val identifier: ResourceLocation = minosoft("enum")

        override fun read(buffer: PlayInByteBuffer) = TODO("Can not construct enum parser yet!")
    }
}
