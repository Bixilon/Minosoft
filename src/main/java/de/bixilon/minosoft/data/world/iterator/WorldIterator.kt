/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.iterator

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.CollisionPredicate
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.CollisionContext
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.EmptyCollisionContext
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.EntityCollisionContext
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidHolder
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2iUtil.EMPTY

class WorldIterator(
    private val iterator: Iterator<Vec3i>,
    private val world: World,
    private var chunk: Chunk? = null,
) : Iterator<BlockPair> {
    private var next: BlockPair? = null


    constructor(aabb: AABB, world: World, chunk: Chunk? = null) : this(aabb.positions(), world, chunk)

    private fun update(): Boolean {
        if (!iterator.hasNext()) return false

        var chunk = this.chunk
        val minY = world.dimension.minY
        val maxY = world.dimension.maxY

        val chunkPosition = Vec2i.EMPTY
        val offset = Vec2i.EMPTY
        for (position in iterator) {
            if (position.y !in minY..maxY) continue
            chunkPosition.x = position.x shr 4
            chunkPosition.y = position.z shr 4

            if (chunk == null) {
                chunk = world.chunks[chunkPosition] ?: continue // TODO: Don't query same chunk multiple times
            } else if (chunk.chunkPosition != chunkPosition) {
                offset.x = chunkPosition.x - chunk.chunkPosition.x
                offset.y = chunkPosition.y - chunk.chunkPosition.y
                chunk = chunk.traceChunk(offset) ?: continue
            }
            if (this.chunk !== chunk) {
                this.chunk = chunk
            }

            val state = chunk[position.x and 0x0F, position.y, position.z and 0x0F] ?: continue
            this.next = BlockPair(position, state, chunk)
            return true
        }

        return false
    }

    override fun hasNext(): Boolean {
        if (next != null) {
            return true
        }
        return update()
    }

    override fun next(): BlockPair {
        var next = this.next
        if (next != null) {
            this.next = null
            return next
        }

        if (!update()) throw IllegalStateException("There is no future!")
        next = this.next ?: Broken("next is null")

        this.next = null
        return next
    }


    fun hasCollisions(fluids: Boolean = true, predicate: CollisionPredicate? = null) = hasCollisions(EmptyCollisionContext, fluids, predicate)
    fun hasCollisions(entity: Entity, fluids: Boolean = true, predicate: CollisionPredicate? = null) = hasCollisions(EntityCollisionContext(entity), fluids, predicate)
    fun hasCollisions(entity: Entity, aabb: AABB, fluids: Boolean = true, predicate: CollisionPredicate? = null) = hasCollisions(EntityCollisionContext(entity, aabb = aabb), fluids, predicate)

    fun hasCollisions(context: CollisionContext, fluids: Boolean = true, predicate: CollisionPredicate? = null): Boolean {
        val aabb = context.aabb
        for ((position, state) in this) {
            if (fluids && (state.block is FluidHolder)) {
                //   val height = state.block.fluid.getHeight(state)
                //   if (position.y + height > aabb.min.y) {
                return true
                //    }
            }
            if (state.block !is CollidableBlock) continue
            if (predicate != null && !predicate.invoke(state)) continue

            val shape = state.block.getCollisionShape(context, position, state, null) ?: continue
            if ((shape + position).intersect(aabb)) {
                return true
            }
        }
        return false
    }
}
