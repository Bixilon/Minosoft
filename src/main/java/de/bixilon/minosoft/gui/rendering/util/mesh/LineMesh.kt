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

package de.bixilon.minosoft.gui.rendering.util.mesh

import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.gui.rendering.chunk.VoxelShape
import de.bixilon.minosoft.gui.rendering.chunk.models.renderable.ElementRenderer
import de.bixilon.minosoft.gui.rendering.util.VecUtil.EMPTY
import de.bixilon.minosoft.util.BitByte.isBit
import de.bixilon.minosoft.util.MMath.positiveNegative
import glm_.vec3.Vec3
import glm_.vec3.Vec3d

class LineMesh(
    var color: RGBColor,
    lineWidth: Float = 0.1f,
) : GenericColorMesh() {
    private var halfLineWidth = lineWidth / 2.0f


    fun drawLine(start: Vec3, end: Vec3, mesh: GenericColorMesh) {
        val direction = (end - start).normalize()
        val normal1 = Vec3(direction.z, direction.z, direction.x - direction.y)
        if (normal1 == Vec3.EMPTY) {
            normal1.x = normal1.z
            normal1.z = direction.z
        }
        normal1.normalizeAssign()
        val normal2 = (direction cross normal1).normalize()
        for (i in 0..4) {
            drawLineQuad(mesh, start, end, direction, normal1, normal2, i.isBit(0), i.isBit(1))
        }
    }

    fun drawLineQuad(mesh: GenericColorMesh, start: Vec3, end: Vec3, direction: Vec3, normal1: Vec3, normal2: Vec3, invertNormal1: Boolean, invertNormal2: Boolean) {
        val normal1Multiplier = invertNormal1.positiveNegative
        val normal2Multiplier = invertNormal2.positiveNegative
        val positions = listOf(
            start + normal2 * normal2Multiplier * halfLineWidth - direction * halfLineWidth,
            start + normal1 * normal1Multiplier * halfLineWidth - direction * halfLineWidth,
            end + normal1 * normal1Multiplier * halfLineWidth + direction * halfLineWidth,
            end + normal2 * normal2Multiplier * halfLineWidth + direction * halfLineWidth,
        )
        for ((_, positionIndex) in ElementRenderer.DRAW_ODER) {
            mesh.addVertex(positions[positionIndex], color)
        }
    }

    fun drawVoxelShape(shape: VoxelShape, blockPosition: Vec3d, mesh: GenericColorMesh, margin: Float = 0.0f) {
        for (aabb in shape) {
            val min = blockPosition + aabb.min - margin
            val max = blockPosition + aabb.max + margin

            fun drawSideQuad(x: Double) {
                drawLine(Vec3(x, min.y, min.z), Vec3(x, max.y, min.z), mesh)
                drawLine(Vec3(x, min.y, min.z), Vec3(x, min.y, max.z), mesh)
                drawLine(Vec3(x, max.y, min.z), Vec3(x, max.y, max.z), mesh)
                drawLine(Vec3(x, min.y, max.z), Vec3(x, max.y, max.z), mesh)
            }

            // left quad
            drawSideQuad(min.x)

            // right quad
            drawSideQuad(max.x)

            // connections between 2 quads
            drawLine(Vec3(min.x, min.y, min.z), Vec3(max.x, min.y, min.z), mesh)
            drawLine(Vec3(min.x, max.y, min.z), Vec3(max.x, max.y, min.z), mesh)
            drawLine(Vec3(min.x, max.y, max.z), Vec3(max.x, max.y, max.z), mesh)
            drawLine(Vec3(min.x, min.y, max.z), Vec3(max.x, min.y, max.z), mesh)
        }
    }
}
