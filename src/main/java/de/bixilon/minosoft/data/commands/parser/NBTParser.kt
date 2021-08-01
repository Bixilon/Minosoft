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
import de.bixilon.minosoft.data.commands.parser.exceptions.nbt.ExpectedPrimitiveTagCommandParseException
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class NBTParser : CommandParser() {

    override fun parse(connection: PlayConnection, properties: ParserProperties?, stringReader: CommandStringReader): Any? {
        return when (this) {
            NBT_PARSER -> {
                stringReader.readNBTTag()
            }
            NBT_TAG_PARSER -> {
                val startPos = stringReader.cursor
                val tag = stringReader.readNBTTag()
                if (tag is Map<*, *>) {
                    throw ExpectedPrimitiveTagCommandParseException(stringReader, stringReader.string.substring(startPos, stringReader.cursor), "Compound Tag is invalid here!")
                }
                tag
            }
            NBT_COMPOUND_PARSER -> stringReader.readNBTCompoundTag()
            else -> {
                null
            }
        }

    }

    companion object {
        val NBT_PARSER = NBTParser()
        val NBT_TAG_PARSER = NBTParser()
        val NBT_COMPOUND_PARSER = NBTParser()
    }
}
