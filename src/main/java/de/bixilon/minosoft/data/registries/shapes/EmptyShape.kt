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

package de.bixilon.minosoft.data.registries.shapes

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kmath.vec.vec3.i.Vec3i
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.registries.shapes.aabb.AbstractAABB
import de.bixilon.minosoft.data.registries.shapes.shape.Shape
import de.bixilon.minosoft.data.registries.shapes.shape.ShapeRaycastHit
import de.bixilon.minosoft.data.world.positions.BlockPosition
import de.bixilon.minosoft.data.world.positions.InChunkPosition
import de.bixilon.minosoft.data.world.positions.InSectionPosition

object EmptyShape : Shape {
    override fun intersects(other: AbstractAABB) = false
    override fun intersects(other: AbstractAABB, offset: BlockPosition) = false

    override fun plus(offset: Vec3d) = EmptyShape
    override fun plus(offset: Vec3i) = EmptyShape

    override fun plus(offset: BlockPosition) = EmptyShape
    override fun plus(offset: InChunkPosition) = EmptyShape
    override fun plus(offset: InSectionPosition) = EmptyShape

    override fun calculateMaxDistance(other: AbstractAABB, maxDistance: Double, axis: Axes) = maxDistance
    override fun calculateMaxDistance(other: AbstractAABB, offset: BlockPosition, maxDistance: Double, axis: Axes) = maxDistance

    override fun raycast(position: Vec3d, direction: Vec3d) = null
}
