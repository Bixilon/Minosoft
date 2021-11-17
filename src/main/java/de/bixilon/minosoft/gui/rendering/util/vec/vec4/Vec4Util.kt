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

package de.bixilon.minosoft.gui.rendering.util.vec.vec4

import glm_.vec4.Vec4

object Vec4Util {

    val Vec4.Companion.MIN: Vec4
        get() = Vec4(Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE)

    val Vec4.Companion.EMPTY: Vec4
        get() = Vec4(0.0f, 0.0f, 0.0f, 0.0f)

    val Vec4.Companion.MAX: Vec4
        get() = Vec4(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE)

    fun FloatArray.dot(x: Float, y: Float, z: Float) = this[0] * x + this[1] * y + this[2] * z + this[3]
}
