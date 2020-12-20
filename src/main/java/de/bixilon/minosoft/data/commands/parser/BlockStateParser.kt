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

import de.bixilon.minosoft.data.commands.parser.exceptions.BlockNotFoundCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.BlockPropertyDuplicatedCommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.CommandParseException
import de.bixilon.minosoft.data.commands.parser.exceptions.UnknownBlockPropertyCommandParseException
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties
import de.bixilon.minosoft.data.mappings.ModIdentifier
import de.bixilon.minosoft.data.mappings.blocks.BlockProperties
import de.bixilon.minosoft.data.mappings.blocks.BlockRotations
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.buffers.ImprovedStringReader

class BlockStateParser : CommandParser() {

    @Throws(CommandParseException::class)
    override fun isParsable(connection: Connection, properties: ParserProperties?, stringReader: ImprovedStringReader) {
        val argument = stringReader.readUntil(ProtocolDefinition.COMMAND_SEPARATOR, "[")
        if (!connection.mapping.doesBlockExist(ModIdentifier(argument.key))) {
            throw BlockNotFoundCommandParseException(stringReader, argument.key)
        }
        if (argument.value == "[" || stringReader.nextChar == '[') {
            stringReader.skipSpaces()
            if (stringReader.nextChar == ']') {
                stringReader.skipChar()
                return
            }

            var rotation: BlockRotations? = null
            val allProperties = HashSet<BlockProperties>()
            while (true) {
                val blockPropertyName = stringReader.readUntil("=").key.replace("\\s", "")
                stringReader.skipSpaces()
                val blockPropertyValuePair = stringReader.readUntil("]", ",")
                val blockPropertyValueName = blockPropertyValuePair.key.replace("\\s", "")
                if (blockPropertyName == "facing" || blockPropertyName == "rotation" || blockPropertyName == "orientation" || blockPropertyName == "axis") {
                    if (rotation != null) {
                        throw BlockPropertyDuplicatedCommandParseException(stringReader, blockPropertyName)
                    }
                    rotation = BlockRotations.ROTATION_MAPPING[blockPropertyValueName]
                    if (rotation == null) {
                        throw UnknownBlockPropertyCommandParseException(stringReader, blockPropertyName)
                    }
                    if (blockPropertyValuePair.value == "]") {
                        break
                    }
                    continue
                }
                val blockPropertyKey = BlockProperties.PROPERTIES_MAPPING[blockPropertyName] ?: throw UnknownBlockPropertyCommandParseException(stringReader, blockPropertyName)
                val blockProperty = blockPropertyKey[blockPropertyValueName] ?: throw UnknownBlockPropertyCommandParseException(stringReader, blockPropertyValueName)

                if (allProperties.contains(blockProperty)) {
                    throw BlockPropertyDuplicatedCommandParseException(stringReader, blockPropertyName)
                }
                // ToDo: check for duplicated keys
                allProperties.add(blockProperty)


                if (blockPropertyValuePair.value == "]") {
                    break
                }
            }
        }
    }

    companion object {
        val BLOCK_STACK_PARSER = BlockStateParser()
    }
}
