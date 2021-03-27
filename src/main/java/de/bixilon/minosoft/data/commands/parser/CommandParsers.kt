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
import de.bixilon.minosoft.data.mappings.ResourceLocation
import org.checkerframework.checker.nullness.qual.Nullable

object CommandParsers {
    private val COMMAND_PARSERS: HashBiMap<ResourceLocation, CommandParser> = HashBiMap.create(
        mapOf(
            ResourceLocation("brigadier:bool") to BooleanParser.BOOLEAN_PARSER,
            ResourceLocation("brigadier:double") to DoubleParser.DOUBLE_PARSER,
            ResourceLocation("brigadier:float") to FloatParser.FLOAT_PARSER,
            ResourceLocation("brigadier:integer") to IntegerParser.INTEGER_PARSER,
            ResourceLocation("brigadier:string") to StringParser.STRING_PARSER,
            ResourceLocation("angle") to AngleParser,
            ResourceLocation("entity") to EntityParser.ENTITY_PARSER,
            ResourceLocation("game_profile") to GameProfileParser.GAME_PROFILE_PARSER,
            ResourceLocation("block_pos") to Vec3iParser,
            ResourceLocation("column_pos") to ColumnPositionParser,
            ResourceLocation("vec3") to Vec3Parser,
            ResourceLocation("vec2") to Vec2Parser,
            ResourceLocation("block_state") to BlockStateParser.BLOCK_STACK_PARSER,
            ResourceLocation("block_predicate") to BlockStateParser.BLOCK_PREDICATE_PARSER,
            ResourceLocation("item_stack") to ItemStackParser.ITEM_STACK_PARSER,
            ResourceLocation("item_predicate") to ItemStackParser.ITEM_PREDICATE_PARSER,
            ResourceLocation("color") to ColorParser,
            ResourceLocation("component") to ComponentParser,
            ResourceLocation("message") to MessageParser.MESSAGE_PARSER,
            ResourceLocation("nbt") to NBTParser.NBT_PARSER,
            // ToDo: nbt_path
            ResourceLocation("objective") to ObjectiveParser,
            // ToDo: objective_criteria
            ResourceLocation("operation") to OperationParser,
            ResourceLocation("particle") to ParticleParser,
            ResourceLocation("rotation") to RotationParser,
            ResourceLocation("scoreboard_slot") to ScoreboardSlotParser,
            ResourceLocation("score_holder") to ScoreHolderParser,
            ResourceLocation("swizzle") to SwizzleParser,
            ResourceLocation("team") to TeamParser,
            ResourceLocation("item_slot") to ItemSlotParser,
            ResourceLocation("resource_location") to ResourceLocationParser,
            ResourceLocation("mob_effect") to ResourceLocationListParser.MOB_EFFECT_PARSER,
            // ToDo: function
            // ToDo: entity_anchor
            ResourceLocation("range") to RangeParser.RANGE_PARSER,
            ResourceLocation("int_range") to IntRangeParser.INT_RANGE_PARSER,
            ResourceLocation("float_range") to FloatRangeParser.FLOAT_RANGE_PARSER,
            ResourceLocation("item_enchantment") to ResourceLocationListParser.ENCHANTMENT_PARSER,
            ResourceLocation("entity_summon") to ResourceLocationListParser.SUMMONABLE_ENTITY_PARSER,
            ResourceLocation("dimension") to ResourceLocationListParser.DIMENSION_EFFECT_PARSER,
            ResourceLocation("uuid") to UUIDParser,
            ResourceLocation("nbt_tag") to NBTParser.NBT_TAG_PARSER,
            ResourceLocation("nbt_compound_tag") to NBTParser.NBT_COMPOUND_PARSER,
            ResourceLocation("time") to TimeParser.TIME_PARSER
        )
    )

    fun getParserInstance(resourceLocation: ResourceLocation): @Nullable CommandParser? {
        return COMMAND_PARSERS[resourceLocation]
    }
}

