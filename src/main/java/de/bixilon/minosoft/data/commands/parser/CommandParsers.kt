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

import com.google.common.collect.HashBiMap
import de.bixilon.minosoft.data.mappings.ModIdentifier

object CommandParsers {
    private val COMMAND_PARSERS: HashBiMap<ModIdentifier, CommandParser> = HashBiMap.create(mapOf(
            ModIdentifier("brigadier:bool") to BooleanParser.BOOLEAN_PARSER,
            ModIdentifier("brigadier:double") to DoubleParser.DOUBLE_PARSER,
            ModIdentifier("brigadier:float") to FloatParser.FLOAT_PARSER,
            ModIdentifier("brigadier:integer") to IntegerParser.INTEGER_PARSER,
            ModIdentifier("brigadier:string") to StringParser.STRING_PARSER,
            ModIdentifier("entity") to EntityParser.ENTITY_PARSER,
            ModIdentifier("score_holder") to ScoreHolderParser.SCORE_HOLDER_PARSER,
            ModIdentifier("range") to RangeParser.RANGE_PARSER,
            ModIdentifier("message") to MessageParser.MESSAGE_PARSER,
            ModIdentifier("item_stack") to ItemStackParser.ITEM_STACK_PARSER
    ))

    fun getParserInstance(identifier: ModIdentifier): CommandParser {
        return COMMAND_PARSERS.getOrDefault(identifier, DummyParser.DUMMY_PARSER)
    }
}

