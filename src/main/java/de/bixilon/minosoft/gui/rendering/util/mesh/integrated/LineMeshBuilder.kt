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

package de.bixilon.minosoft.gui.rendering.util.mesh.integrated

import de.bixilon.kmath.vec.vec3.d.Vec3d
import de.bixilon.kmath.vec.vec3.f.MVec3f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.shapes.aabb.AABB
import de.bixilon.minosoft.data.registries.shapes.aabb.AABBList
import de.bixilon.minosoft.data.registries.shapes.shape.Shape
import de.bixilon.minosoft.data.text.formatting.color.RGBAColor
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.models.util.CuboidUtil
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.quad.QuadConsumer.Companion.iterate

open class LineMeshBuilder(context: RenderContext, estimate: Int = 6) : GenericColorMeshBuilder(context, estimate) {

    fun drawLine(start: Vec3f, end: Vec3f, lineWidth: Float = RenderConstants.DEFAULT_LINE_WIDTH, color: RGBAColor) {
        drawLine(start.x, start.y, start.z, end.x, end.y, end.z, lineWidth, color)
    }

    fun drawLine(startX: Float, startY: Float, startZ: Float, endX: Float, endY: Float, endZ: Float, lineWidth: Float = RenderConstants.DEFAULT_LINE_WIDTH, color: RGBAColor) {
        ensureSize(4)

        val direction = MVec3f(endX - startX, endY - startY, endZ - startZ).apply { normalizeAssign() }
        val normal1 = MVec3f(direction.z, direction.z, direction.x - direction.y)
        if (direction.z == 0.0f && direction.x == 0.0f) {
            normal1.x = normal1.z
            normal1.z = direction.z
        }
        normal1.normalizeAssign()
        val normal2 = (direction cross normal1).apply { normalizeAssign() }

        val halfLineWidth = lineWidth / 2

        direction *= halfLineWidth
        normal1 *= halfLineWidth
        normal2 *= halfLineWidth

        val invertedNormal1 = -normal1
        val invertedNormal2 = -normal2


        drawLineQuad(startX, startY, startZ, endX, endY, endZ, normal1.unsafe, normal2.unsafe, direction.unsafe, color)
        drawLineQuad(startX, startY, startZ, endX, endY, endZ, invertedNormal2.unsafe, normal1.unsafe, direction.unsafe, color)
        drawLineQuad(startX, startY, startZ, endX, endY, endZ, normal2.unsafe, invertedNormal1.unsafe, direction.unsafe, color)
        drawLineQuad(startX, startY, startZ, endX, endY, endZ, invertedNormal1.unsafe, invertedNormal2.unsafe, direction.unsafe, color)
    }

    private fun drawLineQuad(startX: Float, startY: Float, startZ: Float, endX: Float, endY: Float, endZ: Float, normal1: Vec3f, normal2: Vec3f, directionWidth: Vec3f, color: RGBAColor) {
        addVertex(startX - directionWidth.x + normal2.x, startY - directionWidth.y + normal2.y, startZ - directionWidth.z + normal2.z, color)
        addVertex(startX - directionWidth.x + normal1.x, startY - directionWidth.y + normal1.y, startZ - directionWidth.z + normal1.z, color)
        addVertex(endX + directionWidth.x + normal1.x, endY + directionWidth.y + normal1.y, endZ + directionWidth.z + normal1.z, color)
        addVertex(endX + directionWidth.x + normal2.x, endY + directionWidth.y + normal2.y, endZ + directionWidth.z + normal2.z, color)

        addIndexQuad()
    }

    fun drawLazyAABB(aabb: AABB, color: RGBAColor) {
        ensureSize(6)
        val offset = context.camera.offset.offset
        for (direction in Directions.VALUES) {
            val from = Vec3f(aabb.min - offset)
            val to = Vec3f(aabb.max - offset)
            val positions = CuboidUtil.positions(direction, from, to)

            iterate { addVertex(positions, it * Vec3f.LENGTH, color) }
            addIndexQuad()
        }
    }

    fun drawShape(aabb: AABB, lineWidth: Float = RenderConstants.DEFAULT_LINE_WIDTH, color: RGBAColor, margin: Float = 0.0f) {
        ensureSize(12)
        val offset = context.camera.offset.offset
        val min = Vec3f.Companion(aabb.min) - margin - offset
        val max = Vec3f.Companion(aabb.max) + margin - offset

        // left quad
        drawLine(min.x, min.y, min.z, min.x, max.y, min.z, lineWidth, color)
        drawLine(min.x, min.y, min.z, min.x, min.y, max.z, lineWidth, color)
        drawLine(min.x, max.y, min.z, min.x, max.y, max.z, lineWidth, color)
        drawLine(min.x, min.y, max.z, min.x, max.y, max.z, lineWidth, color)


        // right quad
        drawLine(max.x, min.y, min.z, max.x, max.y, min.z, lineWidth, color)
        drawLine(max.x, min.y, min.z, max.x, min.y, max.z, lineWidth, color)
        drawLine(max.x, max.y, min.z, max.x, max.y, max.z, lineWidth, color)
        drawLine(max.x, min.y, max.z, max.x, max.y, max.z, lineWidth, color)

        // connections between 2 quads
        drawLine(min.x, min.y, min.z, max.x, min.y, min.z, lineWidth, color)
        drawLine(min.x, max.y, min.z, max.x, max.y, min.z, lineWidth, color)
        drawLine(min.x, max.y, max.z, max.x, max.y, max.z, lineWidth, color)
        drawLine(min.x, min.y, max.z, max.x, min.y, max.z, lineWidth, color)
    }

    fun drawShape(shape: AABBList, position: Vec3d, lineWidth: Float, color: RGBAColor, margin: Float = 0.0f) {
        for (aabb in shape) {
            drawShape(aabb + position, lineWidth, color, margin)
        }
    }

    fun drawShape(shape: Shape, position: Vec3d, lineWidth: Float, color: RGBAColor, margin: Float = 0.0f) = when (shape) {
        is AABB -> drawShape(shape + position, lineWidth, color, margin)
        is AABBList -> drawShape(shape, position, lineWidth, color, margin)
        else -> Broken("Don't know how to draw shape: $shape")
    }

}
