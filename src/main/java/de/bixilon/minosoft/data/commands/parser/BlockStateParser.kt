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
import de.bixilon.minosoft.data.commands.parser.exceptions.BlockNotFoundCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.BlockPropertyDuplicatedCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.UnknownBlockPropertyCommandParseException
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties
import de.bixilon.minosoft.data.mappings.blocks.BlockProperties
import de.bixilon.minosoft.data.mappings.blocks.BlockRotations
import de.bixilon.minosoft.protocol.network.Connection

class BlockStateParser : CommandParser() {

    @Throws(CommandParseException::class)
    override fun isParsable(connection: Connection, properties: ParserProperties?, stringReader: CommandStringReader) {
        val identifier = stringReader.readModIdentifier()
        if (!connection.mapping.doesBlockExist(identifier.value)) {
            throw BlockNotFoundCommandParseException(stringReader, identifier.key)
        }
        if (stringReader.canRead() && stringReader.peek() == '[') {
            val propertyMap = stringReader.readProperties()

            var rotation: BlockRotations? = null
            val allProperties = HashSet<BlockProperties>()
            for (pair in propertyMap) {

                if (pair.key == "facing" || pair.key == "rotation" || pair.key == "orientation" || pair.key == "axis") {
                    if (rotation != null) {
                        throw BlockPropertyDuplicatedCommandParseException(stringReader, pair.key)
                    }
                    rotation = BlockRotations.ROTATION_MAPPING[pair.value]
                    if (rotation == null) {
                        throw UnknownBlockPropertyCommandParseException(stringReader, pair.value)
                    }
                    continue
                }
                val blockPropertyKey = BlockProperties.PROPERTIES_MAPPING[pair.key] ?: throw UnknownBlockPropertyCommandParseException(stringReader, pair.key)
                val blockProperty = blockPropertyKey[pair.value] ?: throw UnknownBlockPropertyCommandParseException(stringReader, pair.value)
                allProperties.add(blockProperty)
            }

        }
    }

    companion object {
        val BLOCK_STACK_PARSER = BlockStateParser()
    }
}
