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

package de.bixilon.minosoft.gui.rendering.util.mesh.builder

import de.bixilon.kmath.vec.vec2.f.Vec2f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.primitive.floats.AbstractFloatList
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.VertexBuffer
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.struct.MeshStruct
import de.bixilon.minosoft.util.collections.floats.DirectArrayFloatList
import de.bixilon.minosoft.util.collections.floats.FloatListUtil

abstract class MeshBuilder(
    val context: RenderContext,
    private val struct: MeshStruct,
    val primitive: PrimitiveTypes = context.system.quadType,
    var initialCacheSize: Int = 8192,
    data: AbstractFloatList? = null,
) : AbstractVertexConsumer {
    override val order = context.system.legacyQuadOrder
    private var _data = data
    val data: AbstractFloatList
        get() {
            if (_data == null) {
                _data = FloatListUtil.direct(initialCacheSize)
            }
            return _data.unsafeCast()
        }


    protected fun create(): VertexBuffer {
        val data = this.data
        val index = IntArray(data.size / struct.floats) { it }
        val buffer = context.system.createVertexBuffer(struct, data, primitive, index)

        drop()

        return buffer
    }

    open fun bake() = Mesh(create())

    open fun drop() {
        val data = data
        if (data is DirectArrayFloatList) {
            data.unload()
        }
        _data = null
    }


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
        val texturePositions = arrayOf(
            uvStart,
            Vec2f(uvStart.x, uvEnd.y),
            uvEnd,
            Vec2f(uvEnd.x, uvStart.y),
        )
        order.iterate { position, uv -> vertexConsumer.invoke(positions[position], texturePositions[uv]) }
    }

    override fun ensureSize(floats: Int) {
        data.ensureSize(floats)
    }
}
