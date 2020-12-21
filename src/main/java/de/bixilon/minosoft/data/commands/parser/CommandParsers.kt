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
    private val COMMAND_PARSERS: HashBiMap<ModIdentifier, CommandParser> = HashBiMap.create(
        mapOf(
            ModIdentifier("brigadier:bool") to BooleanParser.BOOLEAN_PARSER,
            ModIdentifier("brigadier:double") to DoubleParser.DOUBLE_PARSER,
            ModIdentifier("brigadier:float") to FloatParser.FLOAT_PARSER,
            ModIdentifier("brigadier:integer") to IntegerParser.INTEGER_PARSER,
            ModIdentifier("brigadier:string") to StringParser.STRING_PARSER,
            ModIdentifier("entity") to EntityParser.ENTITY_PARSER,
            // game_profile
            ModIdentifier("block_pos") to BlockPositionParser.BLOCK_POSITION_PARSER,
            ModIdentifier("column_pos") to ColumnPositionParser.COLUMN_POSITION_PARSER,
            ModIdentifier("vec3") to Vec3Parser.VEC3_PARSER,
            ModIdentifier("vec2") to Vec2Parser.VEC2_PARSER,
            ModIdentifier("block_state") to BlockStateParser.BLOCK_STACK_PARSER,
            // block_predicate
            ModIdentifier("item_stack") to ItemStackParser.ITEM_STACK_PARSER,
            // item_predicate
            // color
            // chat component
            ModIdentifier("message") to MessageParser.MESSAGE_PARSER,
            // nbt
            // nbt_pat
            // objective
            // objective_criteria
            // operation
            // particle
            // rotation
            // scoreboard_slot
            ModIdentifier("score_holder") to ScoreHolderParser.SCORE_HOLDER_PARSER,
            // swizzle
            // team
            // item_slot
            ModIdentifier("resource_location") to IdentifierParser.IDENTIFIER_PARSER,
            ModIdentifier("mob_effect") to IdentifierListParser.MOB_EFFECT_PARSER,
            // function
            // entity_anchor
            ModIdentifier("range") to RangeParser.RANGE_PARSER,
            ModIdentifier("int_range") to IntRangeParser.INT_RANGE_PARSER,
            ModIdentifier("float_range") to FloatRangeParser.FLOAT_RANGE_PARSER,
            ModIdentifier("item_enchantment") to IdentifierListParser.ENCHANTMENT_PARSER,
            // entity_summon
            // dimension
            ModIdentifier("uuid") to UUIDParser.UUID_PARSER,
            // nbt_tag
            // nbt_compound_tag
            // time
        )
    )

    fun getParserInstance(identifier: ModIdentifier): CommandParser {
        return COMMAND_PARSERS.getOrDefault(identifier, DummyParser.DUMMY_PARSER)
    }
}

