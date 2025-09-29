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

package de.bixilon.kmath.vec.vec3.f

import de.bixilon.kmath.vec.Vec
import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.util.KUtil.format

interface _Vec3f : Vec {
    val x: Float
    val y: Float
    val z: Float


    operator fun component1() = x
    operator fun component2() = y
    operator fun component3() = z


    fun toArray() = floatArrayOf(x, y, z)
    override fun toText() = BaseComponent("(", x.format(), " ", y.format(), " ", z.format(), ")")


    companion object {
        inline fun _Vec3f.distance2(other: _Vec3f): Float {
            val x = x - other.x
            val y = y - other.y
            val z = z - other.z

            return x * x + y * y + z * z
        }
    }
}
