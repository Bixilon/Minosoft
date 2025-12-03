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

package de.bixilon.minosoft.data.registries.shapes.collision

import de.bixilon.kmath.vec.vec3.d.MVec3d
import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kmath.vec.vec3.i.Vec3i
import de.bixilon.kutil.array.ArrayUtil.cast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.kutil.math.simple.DoubleMath.ceil
import de.bixilon.kutil.math.simple.DoubleMath.floor
import de.bixilon.kutil.memory.allocator.LongAllocator
import de.bixilon.kutil.memory.allocator.TemporaryAllocator
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.CollisionPredicate
import de.bixilon.minosoft.data.registries.blocks.shapes.collision.context.CollisionContext
import de.bixilon.minosoft.data.registries.blocks.state.BlockStateFlags
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.collision.CollidableBlock
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.aabb.AABBIterator
import de.bixilon.minosoft.data.registries.shapes.shape.ShapeRaycastHit
import de.bixilon.minosoft.data.registries.shapes.shape.Shape
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.data.world.chunk.chunk.Chunk
import de.bixilon.minosoft.data.world.iterator.WorldIterator
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.BlockPosition.Companion.clampX
import de.bixilon.minosoft.data.world.positions.BlockPosition.Companion.clampY
import de.bixilon.minosoft.data.world.positions.BlockPosition.Companion.clampZ
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition

class CollisionShape(
    val world: World,
    context: CollisionContext,
    aabb: AABB,
    movement: Vec3d,
    chunk: Chunk?,
    predicate: CollisionPredicate? = null,
) : Shape {
    private var count: Int
    private val positions: LongArray
    private val shapes: Array<Shape>

    private val offset = MVec3d()


    private fun iterator(aabb: AABB, movement: Vec3d): AABBIterator {
        var minX = aabb.min.x - 0.1
        var minY = aabb.min.y - 1.0 // only fences can have a higher box rn
        var minZ = aabb.min.z - 0.1

        var maxX = aabb.max.x + 0.1
        var maxY = aabb.max.y + 0.1
        var maxZ = aabb.max.z + 0.1


        if (movement.x < 0) minX += movement.x else maxX += movement.x
        if (movement.y < 0) minY += movement.y else maxY += movement.y
        if (movement.z < 0) minZ += movement.z else maxZ += movement.z

        val min = BlockPosition(minX.floor.clampX(), minY.floor.clampY(), minZ.floor.clampZ())
        val max = BlockPosition(maxX.ceil.clampX(), maxY.ceil.clampY(), maxZ.ceil.clampZ())

        return AABBIterator(min, max)
    }

    init {
        val aabbs = iterator(aabb, movement)
        val positions = POSITIONS.allocate(aabbs.size)
        val shapes: Array<Shape?> = SHAPES.allocate(aabbs.size)

        var index = 0

        // TODO: add entity collisions (boat, shulker)
        // TODO: add world border collision shape

        for ((position, state, chunk) in WorldIterator(aabbs, world, chunk)) {
            val block = state.block
            if (BlockStateFlags.COLLISIONS !in state.flags || block !is CollidableBlock) continue
            if (predicate != null && !predicate.invoke(state)) continue
            // TODO: filter blocks (e.g. moving piston), whatever that means

            val entity = chunk.getBlockEntity(position.inChunkPosition)

            val shape = when {
                BlockStateFlags.FULL_COLLISION in state.flags -> Shape.FULL
                else -> block.collisionShape ?: block.getCollisionShape(state) ?: block.getCollisionShape(world.session, context, position, state) ?: entity?.let { block.getCollisionShape(world.session, context, position, state, it) }
            } ?: continue

            // TODO: offset shape

            if (position in aabb && shape.intersects(aabb, -position)) {
                continue
            }
            positions[index] = position.raw
            shapes[index] = shape
            index++
        }
        this.shapes = shapes.cast()
        this.count = index
        this.positions = positions
    }

    override fun intersects(other: AABB): Boolean {
        for (index in 0 until count) {
            val position = BlockPosition(this.positions[index])
            val shape = this.shapes[index]

            if (shape.intersects(other, -position)) return true
        }
        return false
    }

    override fun intersects(other: AABB, offset: BlockPosition) = Broken()

    override fun plus(offset: Vec3d) = Broken()
    override fun plus(offset: Vec3i) = Broken()

    override fun plus(offset: BlockPosition) = Broken()
    override fun plus(offset: InChunkPosition) = Broken()
    override fun plus(offset: InSectionPosition) = Broken()

    override fun calculateMaxDistance(other: AABB, offset: Vec3d, maxDistance: Double, axis: Axes): Double {
        var distance = maxDistance


        for (index in 0 until count) {
            this.offset.invoke(offset)
            this.offset += -BlockPosition(this.positions[index])

            val shape = this.shapes[index]

            distance = shape.calculateMaxDistance(other, this.offset.unsafe, distance, axis)
            // TODO: short circuit if distance is already 0.0?
        }

        return distance
    }

    override fun raycast(position: Vec3d, direction: Vec3d) = Broken("This is for collisions only.")

    fun free() {
        POSITIONS.free(this.positions)
        SHAPES.free(this.shapes.unsafeCast())
    }


    companion object {
        private val POSITIONS = LongAllocator()
        private val SHAPES = object : TemporaryAllocator<Array<Shape?>>() {
            override fun getSize(value: Array<Shape?>) = value.size
            override fun create(size: Int): Array<Shape?> = arrayOfNulls(size)
        }
    }
}
