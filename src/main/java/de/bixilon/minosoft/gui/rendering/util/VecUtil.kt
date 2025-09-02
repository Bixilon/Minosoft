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

package de.bixilon.minosoft.gui.rendering.util

import de.bixilon.minosoft.data.world.vec.vec2.i.Vec2i
import de.bixilon.minosoft.data.world.vec.vec3.f.Vec3f
import de.bixilon.minosoft.data.world.vec.vec3.d.Vec3d
import de.bixilon.minosoft.data.world.vec.vec3.i.Vec3i
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.types.properties.offset.RandomOffsetTypes
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.vec.vec3.f.MVec3f
import java.util.*

@Deprecated(message = "Use VecXUtil instead")
object VecUtil {

    val Float.sqr: Float
        get() = this * this

    fun Vec3f.rotate(axis: Vec3f, sin: Float, cos: Float): Vec3f {
        return this * cos + (axis cross this) * sin + axis * (axis dot this) * (1 - cos)
    }

    inline val Int.inSectionHeight: Int
        get() = this and 0x0F

    inline val Int.sectionHeight: Int
        get() = this shr 4

    val Vec3i.centerf: Vec3f
        get() = Vec3f(x + 0.5f, y + 0.5f, z + 0.5f)

    val Vec3i.center: Vec3d
        get() = Vec3d(x + 0.5, y + 0.5, z + 0.5)


    fun BlockPosition.getWorldOffset(offsetType: RandomOffsetTypes): Vec3f {
        val positionHash = hash
        val maxModelOffset = 0.25f // ToDo: PixLyzer: use block.model.max_model_offset

        fun horizontal(axisHash: Long): Float {
            return (((axisHash and 0xF) / 15.0f) - 0.5f) / 2.0f
        }

        val offset = MVec3f(
            x = horizontal(positionHash),
            y = if (offsetType === RandomOffsetTypes.XYZ) {
                (((positionHash shr 4 and 0xF) / 15.0f) - 1.0f) / 5.0f
            } else {
                0.0f
            },
            z = horizontal(positionHash shr 8))

        offset.clampAssign(-maxModelOffset, maxModelOffset)

        return offset.unsafe
    }

    val Vec3d.toVec3: Vec3f
        get() = Vec3f(this)

    val Vec3f.toVec3d: Vec3d
        get() = Vec3d(this)

    fun Double.noised(random: Random): Double {
        return random.nextDouble() / this * if (random.nextBoolean()) 1.0 else -1.0
    }
}
