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
import org.checkerframework.checker.nullness.qual.Nullable

object CommandParsers {
    private val COMMAND_PARSERS: HashBiMap<ModIdentifier, CommandParser> = HashBiMap.create(
        mapOf(
            ModIdentifier("brigadier:bool") to BooleanParser.BOOLEAN_PARSER,
            ModIdentifier("brigadier:double") to DoubleParser.DOUBLE_PARSER,
            ModIdentifier("brigadier:float") to FloatParser.FLOAT_PARSER,
            ModIdentifier("brigadier:integer") to IntegerParser.INTEGER_PARSER,
            ModIdentifier("brigadier:string") to StringParser.STRING_PARSER,
            ModIdentifier("angle") to AngleParser.ANGLE_PARSER,
            ModIdentifier("entity") to EntityParser.ENTITY_PARSER,
            ModIdentifier("game_profile") to GameProfileParser.GAME_PROFILE_PARSER,
            ModIdentifier("block_pos") to BlockPositionParser.BLOCK_POSITION_PARSER,
            ModIdentifier("column_pos") to ColumnPositionParser.COLUMN_POSITION_PARSER,
            ModIdentifier("vec3") to Vec3Parser.VEC3_PARSER,
            ModIdentifier("vec2") to Vec2Parser.VEC2_PARSER,
            ModIdentifier("block_state") to BlockStateParser.BLOCK_STACK_PARSER,
            // block_predicate
            ModIdentifier("item_stack") to ItemStackParser.ITEM_STACK_PARSER,
            // item_predicate
            ModIdentifier("color") to ColorParser.COLOR_PARSER,
            // chat component
            ModIdentifier("message") to MessageParser.MESSAGE_PARSER,
            // nbt
            // nbt_path
            ModIdentifier("objective") to ObjectiveParser.OBJECTIVE_PARSER,
            // objective_criteria
            ModIdentifier("operation") to OperationParser.OPERATION_PARSER,
            // particle
            ModIdentifier("rotation") to RotationParser.ROTATION_PARSER,
            ModIdentifier("scoreboard_slot") to ScoreboardSlotParser.SCOREBOARD_SLOT_PARSER,
            ModIdentifier("score_holder") to ScoreHolderParser.SCORE_HOLDER_PARSER,
            ModIdentifier("swizzle") to SwizzleParser.SWIZZLE_PARSER,
            ModIdentifier("team") to TeamParser.TEAM_PARSER,
            ModIdentifier("item_slot") to ItemSlotParser.ITEM_SLOT_PARSER,
            ModIdentifier("resource_location") to IdentifierParser.IDENTIFIER_PARSER,
            ModIdentifier("mob_effect") to IdentifierListParser.MOB_EFFECT_PARSER,
            // function
            // entity_anchor
            ModIdentifier("range") to RangeParser.RANGE_PARSER,
            ModIdentifier("int_range") to IntRangeParser.INT_RANGE_PARSER,
            ModIdentifier("float_range") to FloatRangeParser.FLOAT_RANGE_PARSER,
            ModIdentifier("item_enchantment") to IdentifierListParser.ENCHANTMENT_PARSER,
            ModIdentifier("entity_summon") to IdentifierListParser.SUMMONABLE_ENTITY_PARSER,
            ModIdentifier("dimension") to IdentifierListParser.DIMENSION_EFFECT_PARSER,
            ModIdentifier("uuid") to UUIDParser.UUID_PARSER,
            // nbt_tag
            // nbt_compound_tag
            ModIdentifier("time") to TimeParser.TIME_PARSER
        )
    )

    fun getParserInstance(identifier: ModIdentifier): @Nullable CommandParser? {
        return COMMAND_PARSERS[identifier]
    }
}

