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

import de.bixilon.kutil.enums.inline.IntInlineSet
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.reflection.ReflectionUtil.getFieldOrNull
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
import kotlin.reflect.KVisibility

object BlockStateFlags {
    const val FLUID = 0
    const val WATERLOGGED = 1
    const val OFFSET = 2
    const val ENTITY = 3

    const val OUTLINE = 4
    const val FULL_OUTLINE = 5

    const val COLLISIONS = 6
    const val FULL_COLLISION = 7

    const val FULL_OPAQUE = 8

    // physics
    const val VELOCITY = 9
    const val JUMP = 10

    // rendering
    const val TINTED = 11
    const val RANDOM_TICKS = 12
    const val CUSTOM_CULLING = 13
    const val MINOR_VISUAL_IMPACT = 14 // TODO: fence, vine, candle, pointed dripstone, amethyst, saplings, coral, sign, rails, sugar cane
    const val CAVE_SURFACE = 15 // TODO: dripstone block, amethyst

    fun of(block: Block): IntInlineSet {
        var flags = IntInlineSet()

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

    fun update(flags: IntInlineSet, properties: Map<BlockProperty<*>, Any>): IntInlineSet {
        var flags = flags

        if (properties[BlockProperties.WATERLOGGED]?.toBoolean() == true) { // TODO: Check if WaterloggableBlock
            flags += WATERLOGGED
            flags += FLUID
        }

        return flags
    }

    fun IntInlineSet.toFlagSet(): Set<String> {
        val result = HashSet<String>()

        for (field in BlockStateFlags::class.members.filter { it.isFinal && it.visibility == KVisibility.PUBLIC }) {
            val java = BlockStateFlags::class.java.getFieldOrNull(field.name) ?: continue
            if (java.type != Int::class.java) continue
            val bit = java.getInt(null)

            if (bit !in this) continue
            result += java.name
        }

        return result
    }
}
