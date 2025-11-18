/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.registries.blocks.state

import de.bixilon.kutil.enums.BitEnumSet
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.kutil.enums.ValuesEnum.Companion.names
import de.bixilon.kutil.enums.inline.enums.IntInlineEnumSet
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.minosoft.data.registries.blocks.light.OpaqueProperty
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperty
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.data.registries.blocks.types.entity.BlockWithEntity
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidHolder
import de.bixilon.minosoft.data.registries.blocks.types.properties.offset.OffsetBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.offset.RandomOffsetBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.physics.JumpBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.physics.VelocityBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.rendering.RandomDisplayTickable
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.outline.OutlinedBlock
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.cull.CustomBlockCulling
import de.bixilon.minosoft.gui.rendering.tint.TintedBlock

enum class BlockStateFlags {
    FLUID,
    WATERLOGGED,
    OFFSET,
    ENTITY,

    OUTLINE,
    FULL_OUTLINE,

    COLLISIONS,
    FULL_COLLISION,

    FULL_OPAQUE,

    // physics
    VELOCITY,
    JUMP,

    // rendering
    TINTED,
    RANDOM_TICKS,
    CUSTOM_CULLING,
    MINOR_VISUAL_IMPACT, // TODO: fence, vine, candle, pointed dripstone, amethyst, saplings, flowers, coral, sign, rails, sugar cane
    CAVE_SURFACE, // TODO: gravel, tuff, dropstone block, amethyst
    ;

    companion object : ValuesEnum<BlockStateFlags> {
        override val VALUES = values()
        override val NAME_MAP = names()

        fun of(block: Block): IntInlineEnumSet<BlockStateFlags> {
            var flags = IntInlineEnumSet<BlockStateFlags>()

            if (block.lightProperties == OpaqueProperty) flags += FULL_OPAQUE

            if (block is FluidHolder) flags += FLUID
            if (block is OffsetBlock && (block !is RandomOffsetBlock || block.randomOffset != null)) flags += OFFSET
            if (block is BlockWithEntity<*>) flags += ENTITY

            if (block is CollidableBlock) {
                flags += COLLISIONS
                if (block.collisionShape == AABB.BLOCK) {
                    flags += FULL_COLLISION
                }
            }
            if (block is OutlinedBlock) {
                flags += OUTLINE
                if (block.outlineShape == AABB.BLOCK) {
                    flags += FULL_OUTLINE
                }
            }
            if (block is VelocityBlock && block.velocity != 1.0f) flags += VELOCITY
            if (block is JumpBlock && block.jumpBoost != 1.0f) flags += JUMP

            if (block is TintedBlock) flags += TINTED
            if (block is RandomDisplayTickable) flags += RANDOM_TICKS
            if (block is CustomBlockCulling) flags += CUSTOM_CULLING

            return flags
        }

        fun update(flags: IntInlineEnumSet<BlockStateFlags>, properties: Map<BlockProperty<*>, Any>): IntInlineEnumSet<BlockStateFlags> {
            var flags = flags

            if (properties[BlockProperties.WATERLOGGED]?.toBoolean() == true) { // TODO: Check if WaterloggableBlock
                flags += WATERLOGGED
                flags += FLUID
            }

            return flags
        }

        fun IntInlineEnumSet<BlockStateFlags>.toSet(): BitEnumSet<BlockStateFlags> {
            val result = set()
            for (flag in VALUES) {
                if (flag !in this) continue
                result += flag
            }

            return result
        }
    }
}
