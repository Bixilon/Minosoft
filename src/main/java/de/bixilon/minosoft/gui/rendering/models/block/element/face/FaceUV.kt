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

package de.bixilon.minosoft.gui.rendering.models.block.element.face

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement.Companion.BLOCK_SIZE
import de.bixilon.minosoft.gui.rendering.models.block.state.baked.BakingUtil.pushRight

data class FaceUV(
    val start: Vec2,
    val end: Vec2,
) {
    constructor(u1: Float, v1: Float, u2: Float, v2: Float) : this(Vec2(u1, v1), Vec2(u2, v2))
    constructor(u1: Int, v1: Int, u2: Int, v2: Int) : this(u1 / BLOCK_SIZE, v1 / BLOCK_SIZE, u2 / BLOCK_SIZE, v2 / BLOCK_SIZE)


    fun toArray(direction: Directions, rotation: Int): FloatArray {
        val floats = when (direction) {
            // @formatter:off
            Directions.DOWN,
            Directions.SOUTH,
            Directions.WEST ->  floatArrayOf(start.x, start.y,   start.x, end.y,     end.x,   end.y,     end.x,   start.y )
            Directions.UP ->    floatArrayOf(start.x, end.y,     end.x,   end.y,     end.x,   start.y,   start.x, start.y )
            Directions.NORTH,
            Directions.EAST ->  floatArrayOf(end.x,   start.y,   start.x, start.y,   start.x, end.y,     end.x,   end.y   )
            // @formatter:on
        }
        if (rotation == 0) return floats
        return floats.pushRight(2, rotation)
    }
}
