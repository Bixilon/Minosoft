/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.commands.parser

import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.ItemNotFoundCommandParseException
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties
import de.bixilon.minosoft.data.mappings.ModIdentifier
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.buffers.ImprovedStringReader

class ItemStackParser : CommandParser() {

    @Throws(CommandParseException::class)
    override fun isParsable(connection: Connection, properties: ParserProperties?, stringReader: ImprovedStringReader) {
        val argument = stringReader.readUntil(ProtocolDefinition.COMMAND_SEPARATOR, "{")
        if (!connection.mapping.doesItemExist(ModIdentifier(argument.key))) {
            throw ItemNotFoundCommandParseException(stringReader, argument.key)
        }
        if (argument.value == "{" || stringReader.nextChar == '{') {
            throw TODO("NBT Data needs to be implemented")
        }
    }

    companion object {
        val ITEM_STACK_PARSER = ItemStackParser()
    }
}
