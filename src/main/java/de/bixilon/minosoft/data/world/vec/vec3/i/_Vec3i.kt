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

package de.bixilon.minosoft.data.world.vec.vec3.i

import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.text.formatting.TextFormattable
import de.bixilon.minosoft.data.world.vec.Vec
import de.bixilon.minosoft.data.world.vec.vec3.f.UnsafeVec3f
import de.bixilon.minosoft.util.KUtil.format

interface _Vec3i : Vec {
    override val unsafe: UnsafeVec3i

    val x: Int
    val y: Int
    val z: Int


    operator fun component1() = x
    operator fun component2() = y
    operator fun component3() = z

    fun toArray() = intArrayOf(x, y, z)
    override fun toText() = BaseComponent("(", x.format(), " ", y.format(), " ", z.format(), ")")
}
