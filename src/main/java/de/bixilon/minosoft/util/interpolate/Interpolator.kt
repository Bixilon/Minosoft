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

package de.bixilon.minosoft.util.interpolate

class Interpolator<T>(
    var initial: T,
    var function: InterpolateFunction<T>,
) {
    var value = initial

    var value0 = initial
    var value1 = initial
    var delta = 0.0f
    var identical = false


    fun push(value: T) {
        value0 = value1
        value1 = value
        this.identical = value0 == value1
        delta = 0.0f
        this.value = value0
    }

    fun add(delta: Float) {
        if (this.delta >= 1.0f) return
        this.delta += delta
        if (this.identical) return

        value = function.interpolate(this.delta, value0, value1)
    }

    fun add(seconds: Float, step: Float) {
        add(seconds / step)
    }
}
