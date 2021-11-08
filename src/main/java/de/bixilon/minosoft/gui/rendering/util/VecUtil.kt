/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.util

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.AbstractDirection
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.AABB
import de.bixilon.minosoft.data.registries.blocks.RandomOffsetTypes
import de.bixilon.minosoft.data.registries.blocks.types.Block
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.func.common.ceil
import glm_.func.common.clamp
import glm_.func.common.floor
import glm_.glm
import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import glm_.vec3.Vec3
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i
import glm_.vec3.Vec3t
import kotlin.math.abs
import kotlin.random.Random

@Deprecated(message = "Use VecXUtil instead")
object VecUtil {

    fun Vec3.clear() {
        x = 0.0f
        y = 0.0f
        z = 0.0f
    }

    infix fun <T : Number> Vec3t<T>.assign(other: Vec3t<T>) {
        x = other.x
        y = other.y
        z = other.z
    }

    @JvmName(name = "times2")
    infix operator fun Vec3d.times(lambda: () -> Double): Vec3d {
        return Vec3d(
            x = x * lambda(),
            y = y * lambda(),
            z = z * lambda(),
        )
    }

    infix operator fun Vec3.times(lambda: () -> Float): Vec3 {
        return Vec3(
            x = x * lambda(),
            y = y * lambda(),
            z = z * lambda(),
        )
    }

    infix fun Vec3.modify(lambda: (Float) -> Float): Vec3 {
        return Vec3(
            x = lambda(x),
            y = lambda(y),
            z = lambda(z),
        )
    }

    infix operator fun Vec3d.plus(lambda: () -> Double): Vec3d {
        return Vec3d(
            x = x + lambda(),
            y = y + lambda(),
            z = z + lambda(),
        )
    }

    fun Vec3d.Companion.of(lambda: () -> Double): Vec3d {
        return Vec3d(
            x = lambda(),
            y = lambda(),
            z = lambda(),
        )
    }

    infix operator fun Vec3i.plus(lambda: () -> Int): Vec3i {
        return Vec3i(
            x = x + lambda(),
            y = y + lambda(),
            z = z + lambda(),
        )
    }

    infix fun Vec3i.plusDouble(double: () -> Double): Vec3d {
        return Vec3d(
            x = x + double(),
            y = y + double(),
            z = z + double(),
        )
    }

    infix operator fun Vec3i.minus(lambda: () -> Int): Vec3i {
        return Vec3i(
            x = x - lambda(),
            y = y - lambda(),
            z = z - lambda(),
        )
    }

    infix operator fun Vec3d.plusAssign(lambda: () -> Double) {
        this assign this + lambda
    }

    infix operator fun Vec3d.timesAssign(lambda: () -> Double) {
        this assign this * lambda
    }

    val Float.sqr: Float
        get() = this * this

    val Vec3.ticks: Vec3
        get() = this / ProtocolDefinition.TICKS_PER_SECOND

    val Vec3.millis: Vec3
        get() = this * ProtocolDefinition.TICKS_PER_SECOND

    val Vec3d.millis: Vec3d
        get() = this * ProtocolDefinition.TICKS_PER_SECOND


    fun Vec3.rotate(axis: Vec3, sin: Float, cos: Float): Vec3 {
        return this * cos + (axis cross this) * sin + axis * (axis dot this) * (1 - cos)
    }

    fun Int.chunkPosition(multiplier: Int): Int {
        return if (this >= 0) {
            this / multiplier
        } else {
            ((this + 1) / multiplier) - 1
        }
    }

    val Vec3i.chunkPosition: Vec2i
        get() = Vec2i(this.x.chunkPosition(ProtocolDefinition.SECTION_WIDTH_X), this.z.chunkPosition(ProtocolDefinition.SECTION_WIDTH_Z))

    fun Int.inChunkPosition(multiplier: Int): Int {
        var coordinate: Int = this % multiplier
        if (coordinate < 0) {
            coordinate += multiplier
        }
        return coordinate
    }

    val Vec3i.inChunkPosition: Vec3i
        get() = Vec3i(this.x.inChunkPosition(ProtocolDefinition.SECTION_WIDTH_X), y, this.z.inChunkPosition(ProtocolDefinition.SECTION_WIDTH_Z))

    val Vec3i.inChunkSectionPosition: Vec3i
        get() {
            val inVec2i = inChunkPosition
            val y = if (y < 0) {
                ((ProtocolDefinition.SECTION_HEIGHT_Y + (y % ProtocolDefinition.SECTION_HEIGHT_Y))) % ProtocolDefinition.SECTION_HEIGHT_Y
            } else {
                y % ProtocolDefinition.SECTION_HEIGHT_Y
            }
            return Vec3i(inVec2i.x, y, inVec2i.z)
        }

    val Vec3i.sectionHeight: Int
        get() {
            return if (y < 0) {
                (y + 1) / ProtocolDefinition.SECTION_HEIGHT_Y - 1
            } else {
                y / ProtocolDefinition.SECTION_HEIGHT_Y
            }
        }

    val Vec3i.entityPosition: Vec3d
        get() = Vec3d(x + 0.5f, y, z + 0.5f) // ToDo: Confirm

    val Vec3.blockPosition: Vec3i
        get() = this.floor

    val Vec3d.blockPosition: Vec3i
        get() = this.floor

    val Vec3i.centerf: Vec3
        get() = Vec3(x + 0.5f, y + 0.5f, z + 0.5f)

    val Vec3i.center: Vec3d
        get() = Vec3d(x + 0.5, y + 0.5, z + 0.5)

    fun Vec3i.Companion.of(chunkPosition: Vec2i, sectionHeight: Int, inChunkSectionPosition: Vec3i): Vec3i {
        return Vec3i(
            chunkPosition.x * ProtocolDefinition.SECTION_WIDTH_X + inChunkSectionPosition.x,
            sectionHeight * ProtocolDefinition.SECTION_HEIGHT_Y + inChunkSectionPosition.y,
            chunkPosition.y * ProtocolDefinition.SECTION_WIDTH_Z + inChunkSectionPosition.z
        ) // ToDo: Confirm
    }

    infix operator fun Vec3i.plus(vec3: Vec3i?): Vec3i {
        if (vec3 == null) {
            return this
        }
        return Vec3i((x + vec3.x), (y + vec3.y), (z + vec3.z))
    }

    infix operator fun Vec3i.plus(vec2: Vec2i?): Vec3i {
        if (vec2 == null) {
            return this
        }
        return Vec3i((x + vec2.x), y, (z + vec2.y))
    }

    infix operator fun Vec3i.plus(direction: AbstractDirection?): Vec3i {
        return this + direction?.vector
    }

    infix operator fun Vec3i.plus(input: Vec3): Vec3 {
        return Vec3(input.x + x, input.y + y, input.z + z)
    }

    infix operator fun Vec2i.plus(vec3: Vec3i): Vec2i {
        return Vec2i(x + vec3.x, y + vec3.z)
    }

    infix operator fun Vec2i.plus(direction: AbstractDirection): Vec2i {
        return this + direction.vector
    }

    fun Vec3i.getWorldOffset(block: Block): Vec3 {
        if (block.randomOffsetType == null || !Minosoft.config.config.game.other.flowerRandomOffset) {
            return Vec3.EMPTY
        }

        val positionHash = generatePositionHash(x, 0, z)
        val maxModelOffset = 0.25f // ToDo: use block.model.max_model_offset

        fun horizontal(axisHash: Long): Float {
            return (((axisHash and 0xF) / 15.0f) - 0.5f) / 2.0f
        }

        return Vec3(
            x = horizontal(positionHash),
            y = if (block.randomOffsetType === RandomOffsetTypes.XYZ) {
                (((positionHash shr 4 and 0xF) / 15.0f) - 1.0f) / 5.0f
            } else {
                0.0f
            },
            z = horizontal(positionHash shr 8)).clamp(-maxModelOffset, maxModelOffset)
    }

    fun Vec3.clamp(min: Float, max: Float): Vec3 {
        return Vec3(
            x = x.clamp(min, max),
            y = y.clamp(min, max),
            z = z.clamp(min, max),
        )
    }

    fun Vec3d.clamp(min: Double, max: Double): Vec3d {
        return Vec3d(
            x = x.clamp(min, max),
            y = y.clamp(min, max),
            z = z.clamp(min, max),
        )
    }

    val Vec3.empty: Boolean
        get() = this.length() < 0.001f

    val Vec3d.empty: Boolean
        get() = this.length() < 0.001

    fun generatePositionHash(x: Int, y: Int, z: Int): Long {
        var hash = (x * 3129871L) xor z.toLong() * 116129781L xor y.toLong()
        hash = hash * hash * 42317861L + hash * 11L
        return hash shr 16
    }

    fun getDistanceToNextIntegerAxisInDirection(position: Vec3d, direction: Vec3d): Double {
        fun getTarget(direction: Vec3d, position: Vec3d, axis: Axes): Int {
            return if (direction[axis] > 0) {
                position[axis].floor.toInt() + 1
            } else {
                position[axis].ceil.toInt() - 1
            }
        }

        fun getLengthMultiplier(direction: Vec3d, position: Vec3d, axis: Axes): Double {
            return (getTarget(direction, position, axis) - position[axis]) / direction[axis]
        }

        val directionXDistance = getLengthMultiplier(direction, position, Axes.X)
        val directionYDistance = getLengthMultiplier(direction, position, Axes.Y)
        val directionZDistance = getLengthMultiplier(direction, position, Axes.Z)
        return glm.min(directionXDistance, directionYDistance, directionZDistance)
    }

    val Vec3.min: Float
        get() = glm.min(this.x, this.y, this.z)

    val Vec3.max: Float
        get() = glm.max(this.x, this.y, this.z)

    val Vec3.signs: Vec3
        get() = Vec3(glm.sign(this.x), glm.sign(this.y), glm.sign(this.z))

    val Vec3.floor: Vec3i
        get() = Vec3i(this.x.floor, this.y.floor, this.z.floor)

    val Vec3d.floor: Vec3i
        get() = Vec3i(this.x.floor, this.y.floor, this.z.floor)

    fun Vec3d.getMinDistanceDirection(aabb: AABB): Directions {
        var minDistance = Double.MAX_VALUE
        var minDistanceDirection = Directions.UP
        fun getDistance(position: Vec3d, direction: Directions): Double {
            val axis = direction.axis
            return (position[axis] - this[axis]) * -direction[axis]
        }
        for (direction in Directions.VALUES) {
            val distance = if (direction[direction.axis] > 0f) {
                getDistance(aabb.max, direction)
            } else {
                getDistance(aabb.min, direction)
            }
            if (distance < minDistance) {
                minDistance = distance
                minDistanceDirection = direction
            }
        }
        return minDistanceDirection
    }

    val <T : Number> Vec3t<T>.toVec3: Vec3
        get() = Vec3(this)

    val <T : Number> Vec3t<T>.toVec3d: Vec3d
        get() = Vec3d(this)

    operator fun <T : Number> Vec3t<T>.get(axis: Axes): T {
        return when (axis) {
            Axes.X -> this.x
            Axes.Y -> this.y
            Axes.Z -> this.z
        }
    }

    fun Vec3d.Companion.horizontal(xz: () -> Double, y: Double): Vec3d {
        return Vec3d(xz(), y, xz())
    }

    fun Vec3d.horizontalPlus(xz: () -> Double, y: Double): Vec3d {
        return Vec3d(this.x + xz(), this.y + y, this.z + xz())
    }

    val Float.noise: Float
        get() = Random.nextFloat() / this * if (Random.nextBoolean()) 1.0f else -1.0f

    val Double.noise: Double
        get() = Random.nextDouble() / this * if (Random.nextBoolean()) 1.0 else -1.0

    fun lerp(delta: Float, start: Vec3, end: Vec3): Vec3 {
        return Vec3(
            lerp(delta, start.x, end.x),
            lerp(delta, start.y, end.y),
            lerp(delta, start.z, end.z),
        )
    }

    fun lerp(delta: Float, start: Vec2, end: Vec2): Vec2 {
        return Vec2(
            lerp(delta, start.x, end.x),
            lerp(delta, start.y, end.y),
        )
    }

    fun lerp(delta: Double, start: Vec3d, end: Vec3d): Vec3d {
        when {
            delta <= 0.0 -> return start
            delta >= 1.0 -> return end
        }
        return Vec3d(
            lerp(delta, start.x, end.x),
            lerp(delta, start.y, end.y),
            lerp(delta, start.z, end.z),
        )
    }

    fun lerp(delta: Float, start: Float, end: Float): Float {
        return start + delta * (end - start)
    }

    fun lerp(delta: Double, start: Double, end: Double): Double {
        return start + delta * (end - start)
    }

    fun Vec3.clearZero() {
        if (abs(x) < 0.003f) {
            x = 0.0f
        }
        if (abs(y) < 0.003f) {
            y = 0.0f
        }
        if (abs(z) < 0.003f) {
            z = 0.0f
        }
    }

    fun Vec3d.clearZero() {
        if (abs(x) < 0.003) {
            x = 0.0
        }
        if (abs(y) < 0.003) {
            y = 0.0
        }
        if (abs(z) < 0.003) {
            z = 0.0
        }
    }

    operator fun AbstractDirection.plus(direction: AbstractDirection): Vec3i {
        return this.vector + direction.vector
    }

    val Vec3.rad: Vec3
        get() = glm.radians(this)
}
