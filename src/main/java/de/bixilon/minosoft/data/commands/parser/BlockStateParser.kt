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
import de.bixilon.minosoft.data.mappings.blocks.BlockRotations
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.mappings.blocks.WannabeBlockState
import de.bixilon.minosoft.data.mappings.blocks.properties.BlockProperties
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
            val allProperties: MutableMap<BlockProperties, Any> = mutableMapOf()
            for ((groupName, value) in propertyMap) {

                if (BlockState.ROTATION_PROPERTIES.contains(groupName)) {
                    if (rotation != null) {
                        throw BlockPropertyDuplicatedCommandParseException(stringReader, groupName)
                    }
                    rotation = BlockRotations.ROTATION_MAPPING[value]
                    if (rotation == null) {
                        throw UnknownBlockPropertyCommandParseException(stringReader, value)
                    }
                    continue
                }
                val (parsedGroup, parsedValue) = try {
                    BlockProperties.parseProperty(groupName, value)
                } catch (exception: Throwable) {
                    throw UnknownBlockPropertyCommandParseException(stringReader, value)
                }
                allProperties[parsedGroup] = parsedValue
            }
            for (state in block.states) {
                if (state.equals(WannabeBlockState(block.resourceLocation, allProperties, rotation))) {
                    blockState = state
                    break
                }
            }
        } else {
            blockState = block.states.iterator().next()
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
