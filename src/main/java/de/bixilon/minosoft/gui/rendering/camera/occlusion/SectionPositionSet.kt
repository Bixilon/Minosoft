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

package de.bixilon.minosoft.gui.rendering.camera.occlusion

import de.bixilon.kutil.bit.set.AbstractBitSet
import de.bixilon.minosoft.data.world.positions.ChunkPosition
import de.bixilon.minosoft.data.world.positions.SectionPosition

class SectionPositionSet private constructor(
    val offset: SectionPosition,
    val size: SectionPosition,
) { // Don't inherit from set, it will box primitives
    private val set = AbstractBitSet.of(size.x * size.y * size.z) // TODO: cache old set one


    constructor(center: ChunkPosition, radius: Int, minSection: Int, sections: Int) : this(SectionPosition(center.x - radius, minSection, center.z - radius), SectionPosition(radius * 2 + 1, sections, radius * 2 + 1))


    operator fun plusAssign(position: SectionPosition) {
        this[position] = true
    }

    operator fun minusAssign(position: SectionPosition) {
        this[position] = false
    }

    operator fun contains(position: SectionPosition): Boolean {
        val index = position.index()
        if (index == INVALID_INDEX) return false
        return set[index]
    }

    operator fun set(position: SectionPosition, value: Boolean) {
        val index = position.index()
        if (index == INVALID_INDEX) throw IndexOutOfBoundsException("Invalid set: $position (offset=$offset, size=$size)")
        set[index] = value
    }

    private fun SectionPosition.index(): Int {
        if (x < offset.x || y < offset.y || z < offset.z) return INVALID_INDEX
        val relative = this - offset
        if (relative.x > size.x || relative.y > size.y || relative.z > size.z) return INVALID_INDEX

        return (relative.y * size.x * size.z) + (relative.z * size.x) + relative.x
    }

    companion object {
        const val INVALID_INDEX = -1
    }
}
