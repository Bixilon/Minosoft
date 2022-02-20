/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.commands.parser

import de.bixilon.minosoft.data.commands.CommandStringReader
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.InvalidItemPredicateCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.resourcelocation.ItemNotFoundCommandParseException
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties
import de.bixilon.minosoft.data.inventory.ItemStackUtil
import de.bixilon.minosoft.data.inventory.stack.ItemStack
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class ItemStackParser : CommandParser() {

    @Throws(CommandParseException::class)
    override fun parse(connection: PlayConnection, properties: ParserProperties?, stringReader: CommandStringReader): ItemStack {
        if (this == ITEM_PREDICATE_PARSER) {
            if (stringReader.peek() != '#') {
                throw InvalidItemPredicateCommandParseException(stringReader, stringReader.read().toString())
            }
            stringReader.skip()
        }
        val argument = stringReader.readResourceLocation() // ToDo: Check predicates
        val item = connection.registries.itemRegistry[argument.value]
        check(item != null) {
            throw ItemNotFoundCommandParseException(stringReader, argument.key)
        }
        var nbt: MutableMap<String, Any> = mutableMapOf()
        if (stringReader.peek() == '{') {
            nbt = stringReader.readNBTCompoundTag()
        }
        return ItemStackUtil.of(item, connection, nbt)
    }

    companion object {
        val ITEM_STACK_PARSER = ItemStackParser()
        val ITEM_PREDICATE_PARSER = ItemStackParser()
    }
}
