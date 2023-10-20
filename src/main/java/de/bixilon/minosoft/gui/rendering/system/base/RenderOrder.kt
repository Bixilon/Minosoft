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

package de.bixilon.minosoft.gui.rendering.system.base

class RenderOrder(
    @JvmField val order: IntArray,
) {
    val size = order.size / 2

    init {
        if (order.size % 2 != 0) throw IllegalStateException("Order must be position=>uv, ...")
    }

    inline fun vertex(index: Int, vertex: (position: Int, uv: Int) -> Unit) {
        vertex.invoke(order[index], order[index + 1])
    }

    inline fun iterate(vertex: (position: Int, uv: Int) -> Unit) {
        var index = 0
        while (index < order.size) {
            this.vertex(index, vertex)
            index += 2
        }
    }

    inline fun iterateReverse(vertex: (position: Int, uv: Int) -> Unit) {
        if (size == 0) return
        this.vertex(0, vertex)

        var index = order.size - 1 - 1 // index, element alignment
        while (index > 1) {
            this.vertex(index, vertex)
            index -= 2
        }
    }
}
