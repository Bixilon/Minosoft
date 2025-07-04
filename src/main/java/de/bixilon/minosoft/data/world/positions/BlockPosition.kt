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

package de.bixilon.minosoft.data.world.positions

import glm_.func.common.clamp
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.text.formatting.TextFormattable
import de.bixilon.minosoft.data.world.positions.BlockPositionUtil.assertPosition
import de.bixilon.minosoft.data.world.positions.BlockPositionUtil.generatePositionHash
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight
import de.bixilon.minosoft.util.KUtil.format

@JvmInline
value class BlockPosition(
    val raw: Long,
) : TextFormattable {

    constructor() : this(0, 0, 0)
    constructor(x: Int, y: Int, z: Int) : this(((y and MASK_Y).toLong() shl SHIFT_Y) or ((z and MASK_Z).toLong() shl SHIFT_Z) or ((x and MASK_X).toLong() shl SHIFT_X)) {
        assertPosition(x, -MAX_X, MAX_X)
        assertPosition(y, MIN_Y, MAX_Y)
        assertPosition(z, -MAX_Z, MAX_Z)
    }

    constructor(position: InChunkPosition) : this(position.x, position.y, position.z)

    inline val x: Int get() = (((raw ushr SHIFT_X).toInt() and MASK_X) shl (Int.SIZE_BITS - BITS_X)) shr (Int.SIZE_BITS - BITS_X)
    inline val y: Int get() = (((raw ushr SHIFT_Y).toInt() and MASK_Y) shl (Int.SIZE_BITS - BITS_Y)) shr (Int.SIZE_BITS - BITS_Y)
    inline val z: Int get() = (((raw ushr SHIFT_Z).toInt() and MASK_Z) shl (Int.SIZE_BITS - BITS_Z)) shr (Int.SIZE_BITS - BITS_Z)

    inline fun modify(other: Long, component: Long, add: Long): BlockPosition {
        val bc = raw and other
        val a = ((raw and component) + add) and component
        return BlockPosition(bc or a)
    }

    inline fun modifyX(modify: Long): BlockPosition {
        return modify((Integer.toUnsignedLong(MASK_Y) shl SHIFT_Y) or (Integer.toUnsignedLong(MASK_Z) shl SHIFT_Z), Integer.toUnsignedLong(MASK_X) shl SHIFT_X, modify)
    }

    inline fun modifyY(modify: Long): BlockPosition {
        return modify((Integer.toUnsignedLong(MASK_X) shl SHIFT_X) or (Integer.toUnsignedLong(MASK_Z) shl SHIFT_Z), Integer.toUnsignedLong(MASK_Y) shl SHIFT_Y, modify)
    }

    inline fun modifyZ(modify: Long): BlockPosition {
        return modify((Integer.toUnsignedLong(MASK_X) shl SHIFT_X) or (Integer.toUnsignedLong(MASK_Y) shl SHIFT_Y), Integer.toUnsignedLong(MASK_Z) shl SHIFT_Z, modify)
    }

    inline fun plusX(): BlockPosition {
        assertPosition(this.x < MAX_X)
        return modifyX(X * 1)
    }

    inline fun plusX(x: Int): BlockPosition {
        assertPosition(this.x + x, -MAX_X, MAX_X)
        return modifyX(X * x)
    }

    inline fun minusX(): BlockPosition {
        assertPosition(this.x > -MAX_X)
        return modifyX(-X * 1)
    }

    inline fun plusY(): BlockPosition {
        assertPosition(this.y < MAX_Y)
        return modifyY(Y * 1)
    }

    inline fun plusY(y: Int): BlockPosition {
        assertPosition(this.y + y, MIN_Y, MAX_Y)
        return modifyY(Y * y)
    }

    inline fun minusY(): BlockPosition {
        assertPosition(this.y > -MAX_Y)
        return modifyY(-Y * 1)
    }

    inline fun plusZ(): BlockPosition {
        assertPosition(this.z < MAX_Y)
        return modifyZ(Z * 1)
    }

    inline fun plusZ(z: Int): BlockPosition {
        assertPosition(this.z + z, -MAX_Z, MAX_Z)
        return modifyZ(Z * z)
    }

    inline fun minusZ(): BlockPosition {
        assertPosition(this.z > -MAX_Z)
        return modifyZ(-Z * 1)
    }

    operator fun get(axis: Axes) = when (axis) {
        Axes.X -> x
        Axes.Y -> y
        Axes.Z -> z
    }

    inline fun with(x: Int = this.x, y: Int = this.y, z: Int = this.z) = BlockPosition(x, y, z)

    inline operator fun plus(value: Int) = BlockPosition(this.x + value, this.y + value, this.z + value)
    inline operator fun minus(value: Int) = BlockPosition(this.x - value, this.y - value, this.z - value)
    inline operator fun times(value: Int) = BlockPosition(this.x * value, this.y * value, this.z * value)
    inline operator fun div(value: Int) = BlockPosition(this.x / value, this.y / value, this.z / value)

    inline operator fun plus(position: BlockPosition) = BlockPosition(this.x + position.x, this.y + position.y, this.z + position.z)
    inline operator fun plus(position: InChunkPosition) = BlockPosition(this.x + position.x, this.y + position.y, this.z + position.z)
    inline operator fun plus(position: InSectionPosition) = BlockPosition(this.x + position.x, this.y + position.y, this.z + position.z)
    inline operator fun minus(position: BlockPosition) = BlockPosition(this.x - position.x, this.y - position.y, this.z - position.z)
    inline operator fun minus(position: InChunkPosition) = BlockPosition(this.x - position.x, this.y - position.y, this.z - position.z)
    inline operator fun minus(position: InSectionPosition) = BlockPosition(this.x - position.x, this.y - position.y, this.z - position.z)

    inline operator fun unaryMinus() = BlockPosition(-this.x, -this.y, -this.z)
    inline operator fun unaryPlus() = this

    inline operator fun plus(direction: Directions) = BlockPosition(this.x + direction.vector.x, this.y + direction.vector.y, this.z + direction.vector.z)
    inline operator fun minus(direction: Directions) = BlockPosition(this.x - direction.vector.x, this.y - direction.vector.y, this.z - direction.vector.z)

    inline infix fun and(mask: Int) = BlockPosition(x and mask, y and mask, z and mask)

    inline val hash get() = generatePositionHash(x, y, z)
    inline val sectionHeight get() = y.sectionHeight
    inline val chunkPosition get() = ChunkPosition(x shr 4, z shr 4)
    inline val sectionPosition get() = SectionPosition(x shr 4, y shr 4, z shr 4)
    inline val inChunkPosition get() = InChunkPosition(x and 0x0F, y, this.z and 0x0F)
    inline val inSectionPosition get() = InSectionPosition(x and 0x0F, y.inSectionHeight, z and 0x0F)

    inline operator fun component1() = x
    inline operator fun component2() = y
    inline operator fun component3() = z

    override fun toText() = "(${this.x.format()} ${this.y.format()} ${this.z.format()})"
    override fun toString() = "b($x $y $z)"

    companion object {
        const val BITS_X = 26
        const val MASK_X = (1 shl BITS_X) - 1
        const val SHIFT_X = 0

        const val BITS_Z = 26
        const val MASK_Z = (1 shl BITS_Z) - 1
        const val SHIFT_Z = BITS_X

        const val BITS_Y = 12
        const val MASK_Y = (1 shl BITS_Y) - 1
        const val SHIFT_Y = BITS_X + BITS_Z

        const val X = 1L shl SHIFT_X
        const val Z = 1L shl SHIFT_Z
        const val Y = 1L shl SHIFT_Y

        const val MAX_X = 30_000_000
        const val MIN_Y = -2048
        const val MAX_Y = 2047
        const val MAX_Z = 30_000_000


        val EMPTY = BlockPosition(0, 0, 0)


        inline fun of(chunk: ChunkPosition, sectionHeight: SectionHeight) = BlockPosition(chunk.x shl 4, sectionHeight shl 4, chunk.z shl 4)
        inline fun of(chunk: ChunkPosition, inChunk: InChunkPosition) = BlockPosition((chunk.x shl 4) or inChunk.x, inChunk.y, (chunk.z shl 4) or inChunk.z)
        inline fun of(chunk: ChunkPosition, sectionHeight: SectionHeight, inSection: InSectionPosition) = BlockPosition((chunk.x shl 4) or inSection.x, (sectionHeight shl 4) or inSection.y, (chunk.z shl 4) or inSection.z)
        inline fun of(section: SectionPosition, inSection: InSectionPosition) = BlockPosition((section.x shl 4) or inSection.x, (section.y shl 4) or inSection.y, (section.z shl 4) or inSection.z)
        inline fun of(section: SectionPosition) = BlockPosition(section.x shl 4, section.y shl 4, section.z shl 4)


        fun Int.clampX() = this.clamp(-MAX_X + 1, MAX_X - 1)
        fun Int.clampY() = this.clamp(+MIN_Y + 1, MAX_Y - 1)
        fun Int.clampZ() = this.clamp(-MAX_Z + 1, MAX_Z - 1)
    }
}
