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

package de.bixilon.minosoft.data.registries.blocks.types.properties.offset

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.world.positions.BlockPosition

interface RandomOffsetBlock : OffsetBlock {
    val randomOffset: RandomOffsetTypes? // TODO: make non nullable


    override fun getShapeOffset(position: BlockPosition) = randomOffset?.let { getShapeOffset(position, it) } ?: Vec3d.EMPTY

    override fun getModelOffset(position: BlockPosition) = randomOffset?.let { getModelOffset(position, it) } ?: Vec3f.EMPTY


    companion object {
        private const val MASK = 0x0F

        private fun horizontal(hash: Int): Float {
            return (((hash and MASK) / 15.0f) - 0.5f) / 2.0f
        }

        private fun vertical(hash: Int): Float {
            return (((hash and MASK) / 15.0f) - 1.0f) / 5.0f
        }

        fun getModelOffset(position: BlockPosition, type: RandomOffsetTypes): Vec3f {
            val hash = position.hash.toInt()

            val y = if (type == RandomOffsetTypes.XYZ) vertical(hash shr 4) else 0.0f

            return Vec3f(horizontal(hash shr 0), y, horizontal(hash shr 8))
        }

        fun getShapeOffset(position: BlockPosition, type: RandomOffsetTypes): Vec3d {
            val hash = position.hash.toInt()

            val y = if (type == RandomOffsetTypes.XYZ) vertical(hash shr 4) else 0.0f

            return Vec3d(horizontal(hash shr 0), y, horizontal(hash shr 8))
        }
    }
}
