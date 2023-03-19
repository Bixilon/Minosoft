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

package de.bixilon.minosoft.commands.parser.minecraft.component

import de.bixilon.minosoft.commands.parser.ArgumentParser
import de.bixilon.minosoft.commands.parser.brigadier.string.StringParser
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.suggestion.Suggestion
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

object ChatComponentParser : ArgumentParser<ChatComponent>, ArgumentParserFactory<ChatComponentParser> {
    override val identifier: ResourceLocation = "minecraft:component".toResourceLocation()
    override val examples: List<Any> = listOf("", "hello", """{"text":"hello world"}""")
    private val parser = StringParser(StringParser.StringModes.GREEDY)

    override fun parse(reader: CommandReader): ChatComponent {
        if (!reader.canPeek()) {
            return ChatComponent.EMPTY
        }
        val pointer = reader.pointer
        try {
            return ChatComponent.of(reader.readJson())
        } catch (ignored: Throwable) {
        }
        reader.pointer = pointer
        return ChatComponent.of(parser.parse(reader))
    }

    override fun getSuggestions(reader: CommandReader): List<Suggestion> = TODO()

    override fun read(buffer: PlayInByteBuffer) = this
}
