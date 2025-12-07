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

package de.bixilon.minosoft.data.world.iterator

import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.CollisionPredicate
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.CollisionContext
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.EntityCollisionContext
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.registries.blocks.types.fluid.FluidHolder
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.aabb.AABBIterator
import de.bixilon.minosoft.data.registries.shapes.shape.Shape
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk

class WorldIterator(
    private val iterator: AABBIterator,
    private val world: World,
    private var chunk: Chunk? = null,
) : Iterator<BlockPair> {
    private var pair: BlockPair? = null
    private var next: BlockPair? = null
    private var revision = -1


    constructor(aabb: AABB, world: World, chunk: Chunk? = null) : this(aabb.positions(), world, chunk)

    private fun update(): Boolean {
        if (world.chunks.chunks.unsafe.isEmpty()) return false
        if (!iterator.hasNext()) return false

        var chunk = this.chunk
        val minY = world.dimension.minY
        val maxY = world.dimension.maxY

        for (position in iterator) {
            if (position.y !in minY..maxY) continue
            val chunkPosition = position.chunkPosition

            if (chunk == null) {
                if (revision == world.chunks.revision) continue // previously found no chunk, can not find it now
                this.revision = world.chunks.revision
                chunk = world.chunks[chunkPosition] ?: continue
            } else if (chunk.position != chunkPosition) {
                chunk = chunk.neighbours.traceChunk(chunkPosition - chunk.position) ?: continue
            }
            if (this.chunk !== chunk) {
                this.chunk = chunk
            }
            // TODO: some fast skip? (if section is empty, can not be in section or chunk is null)

            val state = chunk[position.inChunkPosition] ?: continue

            val pair = pair ?: BlockPair(position, state, chunk)
            this.pair = pair

            pair.position = position
            pair.state = state
            pair.chunk = chunk
            this.next = pair

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


    fun hasCollisions(fluids: Boolean = true, predicate: CollisionPredicate? = null) = hasCollisions(null, null, fluids, predicate)
    fun hasCollisions(entity: Entity, fluids: Boolean = true, predicate: CollisionPredicate? = null) = EntityCollisionContext.of(entity)?.let { hasCollisions(it.aabb, it, fluids, predicate) }
    fun hasCollisions(entity: Entity, aabb: AABB, fluids: Boolean = true, predicate: CollisionPredicate? = null) = hasCollisions(aabb, EntityCollisionContext(entity, aabb = aabb), fluids, predicate)

    fun hasCollisions(aabb: AABB?, context: CollisionContext?, fluids: Boolean = true, predicate: CollisionPredicate? = null): Boolean {
        for ((position, state) in this) {
            if (fluids && (BlockStateFlags.FLUID in state.flags && state.block is FluidHolder)) {
                //   val height = state.block.fluid.getHeight(state)
                //   if (position.y + height > aabb.min.y) {
                return true
                //    }
            }
            if (BlockStateFlags.COLLISIONS !in state.flags || state.block !is CollidableBlock) continue
            if (predicate != null && !predicate.invoke(state)) continue

            val entity = chunk?.getBlockEntity(position.inChunkPosition)

            val block = state.block
            val shape = when {
                BlockStateFlags.FULL_COLLISION in state.flags -> Shape.FULL
                else -> block.collisionShape ?: block.getCollisionShape(state) ?: context?.let { block.getCollisionShape(world.session, context, position, state) ?: entity?.let { block.getCollisionShape(world.session, context, position, state, it) } }
            } ?: continue

            if (aabb == null) return true


            if ((shape + position).intersects(aabb)) {
                return true
            }
        }
        return false
    }
}
