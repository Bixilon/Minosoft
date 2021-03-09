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

import de.bixilon.minosoft.data.commands.CommandStringReader
import de.bixilon.minosoft.data.commands.parser.properties.EntityParserProperties
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.protocol.InByteBuffer

class GameProfileParser(val properties: EntityParserProperties) : EntityParser() {

    override fun readParserProperties(buffer: InByteBuffer?): ParserProperties? {
        return null
    }

    override fun parse(connection: Connection, properties: ParserProperties?, stringReader: CommandStringReader): Any? {
        return super.parse(connection, properties, stringReader)
    }

    companion object {
        val GAME_PROFILE_PARSER = GameProfileParser(EntityParserProperties(false, false)) // ToDo own parser?
    }
}
