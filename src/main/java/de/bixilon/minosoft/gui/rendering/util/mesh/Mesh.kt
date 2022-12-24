/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.FloatVertexBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.util.collections.floats.AbstractFloatList
import de.bixilon.minosoft.util.collections.floats.DirectArrayFloatList
import de.bixilon.minosoft.util.collections.floats.FloatListUtil

abstract class Mesh(
    val context: RenderContext,
    private val struct: MeshStruct,
    private val primitiveType: PrimitiveTypes = context.renderSystem.preferredPrimitiveType,
    var initialCacheSize: Int = 10000,
    val clearOnLoad: Boolean = true,
    data: AbstractFloatList? = null,
    val onDemand: Boolean = false,
) : AbstractVertexConsumer {
    override val order = context.renderSystem.primitiveMeshOrder
    val reversedOrder = order.reversedArray()
    private var _data: AbstractFloatList? = data ?: if (onDemand) null else FloatListUtil.direct(initialCacheSize)
    var data: AbstractFloatList
        get() {
            if (_data == null && onDemand) {
                _data = FloatListUtil.direct(initialCacheSize)
            }
            return _data.unsafeCast()
        }
        set(value) {
            _data = value
        }

    protected lateinit var buffer: FloatVertexBuffer

    var vertices: Int = -1
        protected set

    var state = MeshStates.PREPARING
        protected set


    fun finish() {
        if (state != MeshStates.PREPARING) throw IllegalStateException("Mesh is not preparing: $state")
        val data = this.data
        buffer = context.renderSystem.createVertexBuffer(struct, data, primitiveType)
        state = MeshStates.FINISHED
    }

    fun load() {
        if (state == MeshStates.PREPARING) {
            finish()
        }
        if (state != MeshStates.FINISHED) throw IllegalStateException("Mesh is not finished: $state")
        buffer.init()
        if (clearOnLoad) {
            val data = data
            if (data is DirectArrayFloatList) {
                data.unload()
            }
            _data = null
        }
        vertices = buffer.vertices
        state = MeshStates.LOADED
    }

    fun draw() {
        check(state == MeshStates.LOADED) { "Can not draw $state mesh!" }
        buffer.draw()
    }

    fun unload() {
        check(state == MeshStates.LOADED) { "Can not unload $state mesh!" }
        buffer.unload()
        state = MeshStates.UNLOADED
    }


    fun addXQuad(start: Vec2, x: Float, end: Vec2, uvStart: Vec2 = Vec2(0.0f, 0.0f), uvEnd: Vec2 = Vec2(1.0f, 1.0f), vertexConsumer: (position: Vec3, uv: Vec2) -> Unit) {
        val positions = arrayOf(
            Vec3(x, start.x, start.y),
            Vec3(x, start.x, end.y),
            Vec3(x, end.x, end.y),
            Vec3(x, end.x, start.y),
        )
        addQuad(positions, uvStart, uvEnd, vertexConsumer)
    }

    fun addYQuad(start: Vec2, y: Float, end: Vec2, uvStart: Vec2 = Vec2(0.0f, 0.0f), uvEnd: Vec2 = Vec2(1.0f, 1.0f), vertexConsumer: (position: Vec3, uv: Vec2) -> Unit) {
        val positions = arrayOf(
            Vec3(start.x, y, end.y),
            Vec3(end.x, y, end.y),
            Vec3(end.x, y, start.y),
            Vec3(start.x, y, start.y),
        )
        addQuad(positions, uvStart, uvEnd, vertexConsumer)
    }

    fun addZQuad(start: Vec2, z: Float, end: Vec2, uvStart: Vec2 = Vec2(0.0f, 0.0f), uvEnd: Vec2 = Vec2(1.0f, 1.0f), vertexConsumer: (position: Vec3, uv: Vec2) -> Unit) {
        val positions = arrayOf(
            Vec3(start.x, start.y, z),
            Vec3(start.x, end.y, z),
            Vec3(end.x, end.y, z),
            Vec3(end.x, start.y, z),
        )
        addQuad(positions, uvStart, uvEnd, vertexConsumer)
    }

    fun addQuad(positions: Array<Vec3>, uvStart: Vec2 = Vec2(0.0f, 0.0f), uvEnd: Vec2 = Vec2(1.0f, 1.0f), vertexConsumer: (position: Vec3, uv: Vec2) -> Unit) {
        val texturePositions = arrayOf(
            uvStart,
            Vec2(uvStart.x, uvEnd.y),
            uvEnd,
            Vec2(uvEnd.x, uvStart.y),
        )

        for ((vertexIndex, textureIndex) in order) {
            vertexConsumer.invoke(positions[vertexIndex], texturePositions[textureIndex])
        }
    }

    enum class MeshStates {
        PREPARING,
        FINISHED,
        LOADED,
        UNLOADED,
    }

    companion object {
        val TRIANGLE_TO_QUAD_ORDER = arrayOf(
            0 to 1,
            3 to 2,
            2 to 3,
            2 to 3,
            1 to 0,
            0 to 1,
        )
        val QUAD_TO_QUAD_ORDER = arrayOf(
            0 to 1,
            3 to 2,
            2 to 3,
            1 to 0,
        )
    }
}
