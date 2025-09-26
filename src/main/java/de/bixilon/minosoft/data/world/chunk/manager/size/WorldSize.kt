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

package de.bixilon.minosoft.data.world.chunk.manager.size

import de.bixilon.kmath.vec.vec2.i.MVec2i
import de.bixilon.kmath.vec.vec2.i.Vec2i

data class WorldSize(
    val min: MVec2i = MVec2i(Int.MAX_VALUE, Int.MAX_VALUE),
    val max: MVec2i = MVec2i(Int.MIN_VALUE, Int.MIN_VALUE),
    val size: MVec2i = MVec2i(0, 0),
) {

    fun clear() {
        min.x = Int.MAX_VALUE; min.y = Int.MAX_VALUE
        max.x = Int.MIN_VALUE; max.y = Int.MIN_VALUE
        size.x = 0; size.y = 0
    }
}
