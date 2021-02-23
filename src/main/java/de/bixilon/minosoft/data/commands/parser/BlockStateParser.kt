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
import de.bixilon.minosoft.data.commands.parser.exceptions.*
import de.bixilon.minosoft.data.commands.parser.properties.ParserProperties
import de.bixilon.minosoft.data.mappings.blocks.BlockProperties
import de.bixilon.minosoft.data.mappings.blocks.BlockRotations
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.protocol.network.Connection

class BlockStateParser : CommandParser() {

    @Throws(CommandParseException::class)
    override fun parse(connection: Connection, properties: ParserProperties?, stringReader: CommandStringReader): BlockState? {
        if (this == BLOCK_PREDICATE_PARSER) {
            if (stringReader.peek() != '#') {
                throw InvalidBlockPredicateCommandParseException(stringReader, stringReader.read().toString())
            }
            stringReader.skip()
        }
        val resourceLocation = stringReader.readResourceLocation() // ToDo: check tags
        val block = connection.mapping.blockRegistry.get(resourceLocation.value) ?: throw BlockNotFoundCommandParseException(stringReader, resourceLocation.key)
        var blockState: BlockState? = null

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
            for (state in block.states) {
                if (state.bareEquals(BlockState(block, allProperties, rotation ?: BlockRotations.NONE))) {
                    blockState = state
                    break
                }
            }
        } else {
            blockState = BlockState(block)
        }
        check(blockState != null) {
            throw BlockNotFoundCommandParseException(stringReader, resourceLocation.key)
        }

        if (this == BLOCK_PREDICATE_PARSER) {
            if (stringReader.canRead() && stringReader.peek() == '{') {
                stringReader.readNBTCompoundTag()
            }
            return null // ToDo
        }
        return blockState
    }

    companion object {
        val BLOCK_STACK_PARSER = BlockStateParser()
        val BLOCK_PREDICATE_PARSER = BlockStateParser()
    }
}
