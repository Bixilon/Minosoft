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

package de.bixilon.minosoft.gui.rendering.util.mesh

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.voxel.AbstractVoxelShape
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY

open class LineMesh(context: RenderContext) : GenericColorMesh(context) {

    fun drawLine(start: Vec3, end: Vec3, lineWidth: Float = RenderConstants.DEFAULT_LINE_WIDTH, color: RGBColor) {
        data.ensureSize(4 * order.size * GenericColorMeshStruct.FLOATS_PER_VERTEX)

        val direction = (end - start).normalize()
        val normal1 = Vec3(direction.z, direction.z, direction.x - direction.y)
        if (normal1 == Vec3.EMPTY) {
            normal1.x = normal1.z
            normal1.z = direction.z
        }
        normal1.normalizeAssign()
        val normal2 = (direction cross normal1).normalize()

        val halfLineWidth = lineWidth / 2
        val directionWidth = direction * halfLineWidth

        normal1 *= halfLineWidth
        normal2 *= halfLineWidth

        val invertedNormal1 = normal1 * -1
        val invertedNormal2 = normal2 * -1

        val floatColor = color.rgba.buffer()

        drawLineQuad(start, end, normal1, normal2, directionWidth, floatColor)
        drawLineQuad(start, end, invertedNormal2, normal1, directionWidth, floatColor)
        drawLineQuad(start, end, normal2, invertedNormal1, directionWidth, floatColor)
        drawLineQuad(start, end, invertedNormal1, invertedNormal2, directionWidth, floatColor)
    }

    fun tryDrawLine(start: Vec3, end: Vec3, lineWidth: Float = RenderConstants.DEFAULT_LINE_WIDTH, color: RGBColor, shape: AbstractVoxelShape? = null) {
        if (shape != null && !shape.shouldDrawLine(start, end)) {
            return
        }
        drawLine(start, end, lineWidth, color)
    }

    private fun drawLineQuad(start: Vec3, end: Vec3, normal1: Vec3, normal2: Vec3, directionWidth: Vec3, color: Float) {
        val positions = arrayOf(
            start + normal2 - directionWidth,
            start + normal1 - directionWidth,
            end + normal1 + directionWidth,
            end + normal2 + directionWidth,
        )
        for ((positionIndex, _) in order) {
            addVertex(positions[positionIndex], color)
        }
    }

    fun drawAABB(aabb: AABB, position: Vec3d, lineWidth: Float, color: RGBColor, margin: Float = 0.0f, shape: AbstractVoxelShape? = null) {
        drawAABB(aabb + position, lineWidth, color, margin, shape)
    }

    fun drawLazyAABB(aabb: AABB, color: RGBColor) {
        data.ensureSize(6 * order.size * GenericColorMeshStruct.FLOATS_PER_VERTEX)
        for (direction in Directions.VALUES) {
            val positions = direction.getPositions(Vec3(aabb.min), Vec3(aabb.max))
            for ((positionIndex, _) in order) {
                addVertex(positions[positionIndex], color)
            }
        }
    }

    fun drawAABB(aabb: AABB, lineWidth: Float = RenderConstants.DEFAULT_LINE_WIDTH, color: RGBColor, margin: Float = 0.0f, shape: AbstractVoxelShape? = null) {
        data.ensureSize(12 * 4 * order.size * GenericColorMeshStruct.FLOATS_PER_VERTEX)
        val min = aabb.min - margin
        val max = aabb.max + margin

        fun tryDrawLine(start: Vec3, end: Vec3) {
            tryDrawLine(start, end, lineWidth, color, shape)
        }

        fun drawSideQuad(x: Double) {
            tryDrawLine(Vec3(x, min.y, min.z), Vec3(x, max.y, min.z))
            tryDrawLine(Vec3(x, min.y, min.z), Vec3(x, min.y, max.z))
            tryDrawLine(Vec3(x, max.y, min.z), Vec3(x, max.y, max.z))
            tryDrawLine(Vec3(x, min.y, max.z), Vec3(x, max.y, max.z))
        }

        // left quad
        drawSideQuad(min.x)

        // right quad
        drawSideQuad(max.x)

        // connections between 2 quads
        tryDrawLine(Vec3(min.x, min.y, min.z), Vec3(max.x, min.y, min.z))
        tryDrawLine(Vec3(min.x, max.y, min.z), Vec3(max.x, max.y, min.z))
        tryDrawLine(Vec3(min.x, max.y, max.z), Vec3(max.x, max.y, max.z))
        tryDrawLine(Vec3(min.x, min.y, max.z), Vec3(max.x, min.y, max.z))
    }

    fun drawVoxelShape(shape: AbstractVoxelShape, position: Vec3d, lineWidth: Float, color: RGBColor, margin: Float = 0.0f) {
        val aabbs = shape.aabbs
        if (aabbs == 0) {
            return
        }
        if (aabbs == 1) {
            return drawAABB(shape.first(), position, lineWidth, color)
        }
        val positionedShape = shape + position

        for (aabb in shape) {
            drawAABB(aabb + position, lineWidth, color, margin, positionedShape)
        }
    }
}
