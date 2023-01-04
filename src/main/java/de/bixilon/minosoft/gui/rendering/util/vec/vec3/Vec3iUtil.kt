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

package de.bixilon.minosoft.gui.rendering.util.vec.vec3

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.primitive.IntUtil.toInt
import de.bixilon.minosoft.gui.rendering.util.VecUtil.inSectionHeight
import de.bixilon.minosoft.gui.rendering.util.VecUtil.sectionHeight

object Vec3iUtil {

    val Vec3i.Companion.MIN: Vec3i
        get() = Vec3i(Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE)

    val Vec3i.Companion.EMPTY: Vec3i
        get() = Vec3i(0, 0, 0)

    val Vec3i.Companion.MAX: Vec3i
        get() = Vec3i(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE)

    @Deprecated("chunk data types")
    val Vec3i.sectionHeight: Int
        get() = y.sectionHeight

    @Deprecated("chunk data types")
    val Vec3i.chunkPosition: Vec2i
        get() = Vec2i(x shr 4, z shr 4)

    @Deprecated("chunk data types")
    val Vec3i.inChunkPosition: Vec3i
        get() = Vec3i(x and 0x0F, y, this.z and 0x0F)

    @Deprecated("chunk data types")
    val Vec3i.inChunkSectionPosition: Vec3i
        get() = Vec3i(x and 0x0F, y.inSectionHeight, z and 0x0F)

    fun Vec3i.toVec3(): Vec3 {
        val array = array
        return Vec3(floatArrayOf(array[0].toFloat(), array[1].toFloat(), array[2].toFloat()))
    }

    fun Any?.toVec3i(default: Vec3i? = null): Vec3i {
        return toVec3iN() ?: default ?: throw IllegalArgumentException("Not a Vec3i: $this")
    }

    fun Any?.toVec3iN(): Vec3i? {
        return when (this) {
            is List<*> -> Vec3i(this[0].toInt(), this[1].toInt(), this[2].toInt())
            is Map<*, *> -> Vec3i(this["x"]?.toInt() ?: 0, this["y"]?.toInt() ?: 0, this["z"]?.toInt() ?: 0)
            is IntArray -> Vec3i(this[0], this[1], this[2])
            is Number -> Vec3i(this.toInt())
            else -> null
        }
    }

    fun Vec3i.max(value: Int): Vec3i {
        return Vec3i(maxOf(value, x), maxOf(value, y), maxOf(value, z))
    }

    fun Vec3i.length2(): Int {
        return x * x + y * y + z * z
    }
}
