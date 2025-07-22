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

package de.bixilon.minosoft.data.world.vec.vec4.f

import de.bixilon.minosoft.data.text.BaseComponent
import de.bixilon.minosoft.data.world.vec.UnsafeVec
import de.bixilon.minosoft.util.KUtil.format

data class UnsafeVec4f(
    var x: Float,
    var y: Float,
    var z: Float,
    var w: Float,
) : UnsafeVec {

    fun final() = Vec4f(this)
    fun mutable() = MVec4f(this)

    override fun toText() = BaseComponent("(", x.format(), " ", y.format(), " ", z.format(), " ", w.format(), ")")
    override fun toString() = "($x $y $z $w)"
}
