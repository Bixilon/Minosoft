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

package de.bixilon.minosoft.commands.parser.minosoft.connection

import de.bixilon.minosoft.commands.errors.ExpectedArgumentError
import de.bixilon.minosoft.commands.parser.ArgumentParser
import de.bixilon.minosoft.commands.parser.brigadier._int.IntParser.Companion.readInt
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.parser.minecraft.target.targets.selector.error.InvalidSelectorKeyError
import de.bixilon.minosoft.commands.parser.minosoft.connection.identifier.ConnectionId
import de.bixilon.minosoft.commands.parser.minosoft.connection.selector.SelectorConnectionTarget
import de.bixilon.minosoft.commands.parser.minosoft.connection.selector.properties.ConnectionTargetProperties
import de.bixilon.minosoft.commands.parser.minosoft.connection.selector.properties.ConnectionTargetProperty
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.commands.util.ReadResult
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

object ConnectionParser : ArgumentParser<ConnectionTarget>, ArgumentParserFactory<ConnectionParser> {
    override val RESOURCE_LOCATION: ResourceLocation = "minosoft:connection".toResourceLocation()
    override val examples: List<Any?> = listOf("1", "@")
    override val placeholder = ChatComponent.of("<connection>")

    override fun parse(reader: CommandReader): ConnectionTarget {
        if (!reader.canPeek()) {
            throw ExpectedArgumentError(reader)
        }
        return if (reader.peek() == '@'.code) {
            reader.parseSelector()
        } else {
            parseConnectionId(reader)
        }
    }

    fun CommandReader.parseSelector(): SelectorConnectionTarget {
        unsafeRead('@'.code)

        val properties: Map<String, ConnectionTargetProperty> = readMap({ readKey() }, { readValue(it) }) ?: emptyMap()

        return SelectorConnectionTarget(properties)
    }

    private fun CommandReader.readKey(): String? {
        if (peek() == '"'.code) {
            return readUnquotedString()
        }
        return readUntil('='.code)
    }

    private fun CommandReader.readValue(key: ReadResult<String>): ConnectionTargetProperty {
        val target = ConnectionTargetProperties[key.result] ?: throw InvalidSelectorKeyError(this, key)
        return target.read(this)
    }

    fun parseConnectionId(reader: CommandReader): ConnectionTarget {
        val result = reader.readResult { reader.readInt() }
        if (result.result == null) {
            throw ExpectedArgumentError(reader)
        }
        return ConnectionId(result.result)
    }

    override fun getSuggestions(reader: CommandReader): List<Any?> {
        return examples // ToDo
    }

    override fun read(buffer: PlayInByteBuffer): ConnectionParser = this
}
