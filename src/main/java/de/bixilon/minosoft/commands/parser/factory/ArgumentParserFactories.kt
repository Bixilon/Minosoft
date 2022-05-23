/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.commands.parser.factory

import de.bixilon.minosoft.commands.parser.brigadier._double.DoubleParser
import de.bixilon.minosoft.commands.parser.brigadier._float.FloatParser
import de.bixilon.minosoft.commands.parser.brigadier._int.IntParser
import de.bixilon.minosoft.commands.parser.brigadier._long.LongParser
import de.bixilon.minosoft.commands.parser.brigadier.bool.BooleanParser
import de.bixilon.minosoft.commands.parser.brigadier.string.StringParser
import de.bixilon.minosoft.commands.parser.minecraft.coordinate.angle.AngleParser
import de.bixilon.minosoft.commands.parser.minecraft.coordinate.block.BlockPositionParser
import de.bixilon.minosoft.commands.parser.minecraft.coordinate.rotation.RotationParser
import de.bixilon.minosoft.commands.parser.minecraft.coordinate.vec2.Vec2Parser
import de.bixilon.minosoft.commands.parser.minecraft.coordinate.vec3.Vec3Parser
import de.bixilon.minosoft.commands.parser.minecraft.range.RangeParserFactory
import de.bixilon.minosoft.commands.parser.minecraft.range._float.FloatRangeParser
import de.bixilon.minosoft.commands.parser.minecraft.range._int.IntRangeParser
import de.bixilon.minosoft.commands.parser.minecraft.resource.ResourceParser
import de.bixilon.minosoft.commands.parser.minecraft.resource.location.ResourceLocationParser
import de.bixilon.minosoft.commands.parser.minecraft.resource.tag.ResourceOrTagParser
import de.bixilon.minosoft.commands.parser.minecraft.score.holder.ScoreHolderParser
import de.bixilon.minosoft.commands.parser.minecraft.target.TargetParser
import de.bixilon.minosoft.commands.parser.minecraft.time.TimeParser
import de.bixilon.minosoft.commands.parser.minecraft.uuid.UUIDParser
import de.bixilon.minosoft.commands.parser.minosoft.dummy.DummyParser
import de.bixilon.minosoft.data.registries.factory.DefaultFactory

object ArgumentParserFactories : DefaultFactory<ArgumentParserFactory<*>>(
    BooleanParser,
    FloatParser,
    DoubleParser,
    IntParser,
    LongParser,
    StringParser,

    TargetParser,

    RangeParserFactory,
    FloatRangeParser,
    IntRangeParser,

    UUIDParser,
    ResourceLocationParser,
    TimeParser,

    Vec2Parser,
    Vec3Parser,
    BlockPositionParser,
    AngleParser,
    RotationParser,

    DummyParser,

    ScoreHolderParser,
    ResourceParser,
    ResourceOrTagParser,

/* TODO:
minecraft:game_profile
minecraft:column_pos
minecraft:block_state
minecraft:block_predicate
minecraft:item_stack
minecraft:item_predicate
minecraft:color
minecraft:component
minecraft:message
minecraft:nbt
minecraft:nbt_path
minecraft:objective
minecraft:objective_criteria
minecraft:operation
minecraft:particle
minecraft:scoreboard_slot
minecraft:score_holder
minecraft:swizzle
minecraft:team
minecraft:item_slot
minecraft:mob_effect
minecraft:function
minecraft:entity_anchor
minecraft:item_enchantment
minecraft:entity_summon
minecraft:dimension
minecraft:nbt_tag
minecraft:nbt_compound_tag
minecraft:resource_or_tag
minecraft:resource
 */
)
