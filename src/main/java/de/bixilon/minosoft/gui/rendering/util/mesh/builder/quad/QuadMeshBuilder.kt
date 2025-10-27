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

package de.bixilon.minosoft.gui.rendering.util.mesh.builder.quad

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.collections.primitive.floats.FloatList
import de.bixilon.kutil.collections.primitive.ints.IntList
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.util.mesh.builder.MeshBuilder
import de.bixilon.minosoft.gui.rendering.util.mesh.struct.MeshStruct

abstract class QuadMeshBuilder(
    context: RenderContext,
    struct: MeshStruct,
    estimate: Int = 8192,
    data: FloatList? = null,
    index: IntList? = null,
) : MeshBuilder(context, struct, if (context.preferQuads) PrimitiveTypes.QUAD else PrimitiveTypes.TRIANGLE, estimate, data, index), QuadConsumer {
    private val remap = !context.preferQuads

    inline fun addXQuad(start: Vec2f, x: Float, end: Vec2f, uvStart: Vec2f = Vec2f.EMPTY, uvEnd: Vec2f = Vec2f.ONE, vertexConsumer: (position: Vec3f, uv: Vec2f) -> Unit) {
        val positions = arrayOf(
            Vec3f(x, start.x, start.y),
            Vec3f(x, start.x, end.y),
            Vec3f(x, end.x, end.y),
            Vec3f(x, end.x, start.y),
        )
        addQuad(positions, uvStart, uvEnd, vertexConsumer)
    }

    inline fun addYQuad(start: Vec2f, y: Float, end: Vec2f, uvStart: Vec2f = Vec2f.EMPTY, uvEnd: Vec2f = Vec2f.ONE, vertexConsumer: (position: Vec3f, uv: Vec2f) -> Unit) {
        val positions = arrayOf(
            Vec3f(start.x, y, end.y),
            Vec3f(end.x, y, end.y),
            Vec3f(end.x, y, start.y),
            Vec3f(start.x, y, start.y),
        )
        addQuad(positions, uvStart, uvEnd, vertexConsumer)
    }

    inline fun addZQuad(start: Vec2f, z: Float, end: Vec2f, uvStart: Vec2f = Vec2f.EMPTY, uvEnd: Vec2f = Vec2f.ONE, vertexConsumer: (position: Vec3f, uv: Vec2f) -> Unit) {
        val positions = arrayOf(
            Vec3f(start.x, start.y, z),
            Vec3f(start.x, end.y, z),
            Vec3f(end.x, end.y, z),
            Vec3f(end.x, start.y, z),
        )
        addQuad(positions, uvStart, uvEnd, vertexConsumer)
    }

    inline fun addQuad(positions: Array<Vec3f>, uvStart: Vec2f = Vec2f.EMPTY, uvEnd: Vec2f = Vec2f.ONE, vertexConsumer: (position: Vec3f, uv: Vec2f) -> Unit) {
        // TODO: verify render order
        vertexConsumer.invoke(positions[0], uvStart)
        vertexConsumer.invoke(positions[1], Vec2f(uvStart.x, uvEnd.y))
        vertexConsumer.invoke(positions[2], uvEnd)
        vertexConsumer.invoke(positions[3], Vec2f(uvEnd.x, uvStart.y))

        addIndexQuad()
    }

    override fun addIndexQuad(front: Boolean, reverse: Boolean) {
        var offset = (_data?.size ?: (PrimitiveTypes.QUAD.vertices * struct.floats)) / struct.floats // TODO: cleanup
        offset -= PrimitiveTypes.QUAD.vertices
        assert(offset >= 0)

        if (remap) {
            IndexUtil.addTriangleQuad(index, offset, front, reverse)
        } else {
            // That could be left out (=> no index buffer)
            IndexUtil.addNativeQuad(index, offset, front, reverse)
        }
    }
}
