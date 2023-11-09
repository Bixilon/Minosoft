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
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.voxel.AbstractVoxelShape
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.models.util.CuboidUtil
import de.bixilon.minosoft.gui.rendering.system.base.MeshUtil.buffer

open class LineMesh(context: RenderContext, initialCacheSize: Int = 1000) : GenericColorMesh(context, initialCacheSize = initialCacheSize) {

    fun drawLine(start: Vec3, end: Vec3, lineWidth: Float = RenderConstants.DEFAULT_LINE_WIDTH, color: RGBColor) {
        drawLine(start.x, start.y, start.z, end.x, end.y, end.z, lineWidth, color)
    }

    fun drawLine(startX: Float, startY: Float, startZ: Float, endX: Float, endY: Float, endZ: Float, lineWidth: Float = RenderConstants.DEFAULT_LINE_WIDTH, color: RGBColor) {
        data.ensureSize(4 * order.size * GenericColorMeshStruct.FLOATS_PER_VERTEX)

        val direction = Vec3(endX - startX, endY - startY, endZ - startZ).normalizeAssign()
        val normal1 = Vec3(direction.z, direction.z, direction.x - direction.y)
        if (direction.z == 0.0f && direction.x == 0.0f) {
            normal1.x = normal1.z
            normal1.z = direction.z
        }
        normal1.normalizeAssign()
        val normal2 = (direction cross normal1).normalizeAssign()

        val halfLineWidth = lineWidth / 2

        direction *= halfLineWidth
        normal1 *= halfLineWidth
        normal2 *= halfLineWidth

        val invertedNormal1 = normal1 * -1
        val invertedNormal2 = normal2 * -1

        val floatColor = color.rgba.buffer()

        drawLineQuad(startX, startY, startZ, endX, endY, endZ, normal1, normal2, direction, floatColor)
        drawLineQuad(startX, startY, startZ, endX, endY, endZ, invertedNormal2, normal1, direction, floatColor)
        drawLineQuad(startX, startY, startZ, endX, endY, endZ, normal2, invertedNormal1, direction, floatColor)
        drawLineQuad(startX, startY, startZ, endX, endY, endZ, invertedNormal1, invertedNormal2, direction, floatColor)
    }

    fun tryDrawLine(start: Vec3, end: Vec3, lineWidth: Float = RenderConstants.DEFAULT_LINE_WIDTH, color: RGBColor, shape: AbstractVoxelShape? = null) {
        if (shape != null && !shape.shouldDrawLine(start, end)) {
            return
        }
        drawLine(start.x, start.y, start.z, end.x, end.y, end.z, lineWidth, color)
    }

    fun tryDrawLine(startX: Float, startY: Float, startZ: Float, endX: Float, endY: Float, endZ: Float, lineWidth: Float = RenderConstants.DEFAULT_LINE_WIDTH, color: RGBColor, shape: AbstractVoxelShape? = null) {
        if (shape != null) { // && !shape.shouldDrawLine(startX, startY, startZ, endX, endY, endZ)
            return
        }
        drawLine(startX, startY, startZ, endX, endY, endZ, lineWidth, color)
    }

    private fun drawLineQuad(startX: Float, startY: Float, startZ: Float, endX: Float, endY: Float, endZ: Float, normal1: Vec3, normal2: Vec3, directionWidth: Vec3, color: Float) {
        val a = Vec3(startX - directionWidth.x, startY - directionWidth.y, startZ - directionWidth.z)
        val b = Vec3(endX + directionWidth.x, endY + directionWidth.y, endZ + directionWidth.z)

        order.iterate { position, _ ->
            val normal = when (position) {
                0, 3 -> normal2
                1, 2 -> normal1
                else -> Broken()
            }
            val position = when (position) {
                0, 1 -> a
                2, 3 -> b
                else -> Broken()
            }
            addVertex(position.x + normal.x, position.y + normal.y, position.z + normal.z, color)
        }
    }

    fun drawAABB(aabb: AABB, position: Vec3d, lineWidth: Float, color: RGBColor, margin: Float = 0.0f, shape: AbstractVoxelShape? = null) {
        drawAABB(aabb + position, lineWidth, color, margin, shape)
    }

    fun drawLazyAABB(aabb: AABB, color: RGBColor) {
        data.ensureSize(6 * order.size * GenericColorMeshStruct.FLOATS_PER_VERTEX)
        val offset = context.camera.offset.offset
        for (direction in Directions.VALUES) {
            val from = Vec3(aabb.min - offset)
            val to = Vec3(aabb.max - offset)
            val positions = CuboidUtil.positions(direction, from, to)

            order.iterate { position, _ -> addVertex(positions, position * Vec3.length, color) }
        }
    }

    fun drawAABB(aabb: AABB, lineWidth: Float = RenderConstants.DEFAULT_LINE_WIDTH, color: RGBColor, margin: Float = 0.0f, shape: AbstractVoxelShape? = null) {
        data.ensureSize(12 * 4 * order.size * GenericColorMeshStruct.FLOATS_PER_VERTEX)
        val offset = context.camera.offset.offset
        val min = Vec3(aabb.min) - margin - offset
        val max = Vec3(aabb.max) + margin - offset

        // left quad
        tryDrawLine(min.x, min.y, min.z, min.x, max.y, min.z, lineWidth, color, shape)
        tryDrawLine(min.x, min.y, min.z, min.x, min.y, max.z, lineWidth, color, shape)
        tryDrawLine(min.x, max.y, min.z, min.x, max.y, max.z, lineWidth, color, shape)
        tryDrawLine(min.x, min.y, max.z, min.x, max.y, max.z, lineWidth, color, shape)


        // right quad
        tryDrawLine(max.x, min.y, min.z, max.x, max.y, min.z, lineWidth, color, shape)
        tryDrawLine(max.x, min.y, min.z, max.x, min.y, max.z, lineWidth, color, shape)
        tryDrawLine(max.x, max.y, min.z, max.x, max.y, max.z, lineWidth, color, shape)
        tryDrawLine(max.x, min.y, max.z, max.x, max.y, max.z, lineWidth, color, shape)

        // connections between 2 quads
        tryDrawLine(min.x, min.y, min.z, max.x, min.y, min.z, lineWidth, color, shape)
        tryDrawLine(min.x, max.y, min.z, max.x, max.y, min.z, lineWidth, color, shape)
        tryDrawLine(min.x, max.y, max.z, max.x, max.y, max.z, lineWidth, color, shape)
        tryDrawLine(min.x, min.y, max.z, max.x, min.y, max.z, lineWidth, color, shape)
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
