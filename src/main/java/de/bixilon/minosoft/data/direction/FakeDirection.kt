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

package de.bixilon.minosoft.data.direction

import de.bixilon.minosoft.gui.rendering.util.VecUtil.plus
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3d
import glm_.vec3.Vec3
import glm_.vec3.Vec3d
import glm_.vec3.Vec3i

class FakeDirection(override val vector: Vec3i) : AbstractDirection {
    override val vectorf: Vec3 = vector.toVec3
    override val vectord: Vec3d = vector.toVec3d

    companion object {
        val NORTH_WEST = FakeDirection(Directions.NORTH + Directions.WEST)
        val NORTH_EAST = FakeDirection(Directions.NORTH + Directions.EAST)
        val SOUTH_WEST = FakeDirection(Directions.SOUTH + Directions.WEST)
        val SOUTH_EAST = FakeDirection(Directions.SOUTH + Directions.EAST)
    }
}
