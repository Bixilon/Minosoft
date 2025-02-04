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

package de.bixilon.minosoft.gui.rendering.util.mesh

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.collections.primitive.floats.AbstractFloatList
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.FloatVertexBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.util.vec.vec2.Vec2Util
import de.bixilon.minosoft.util.collections.floats.DirectArrayFloatList
import de.bixilon.minosoft.util.collections.floats.FloatListUtil

abstract class Mesh(
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

    protected lateinit var buffer: FloatVertexBuffer

    var vertices: Int = -1
        protected set

    var state = MeshStates.WAITING
        protected set


    fun preload() {
        if (state != MeshStates.WAITING) throw InvalidMeshState(state)
        val data = this.data
        buffer = context.system.createVertexBuffer(struct, data, primitive)
        state = MeshStates.PRELOADED
    }

    fun load() {
        if (state == MeshStates.WAITING) {
            preload()
        }
        if (state != MeshStates.PRELOADED) throw InvalidMeshState(state)
        buffer.init()
        clear()

        vertices = buffer.vertices
        state = MeshStates.LOADED
    }

    protected open fun clear() {
        val data = data
        if (data is DirectArrayFloatList) {
            data.unload()
        }
        _data = null
    }

    fun draw() {
        if (state != MeshStates.LOADED) throw InvalidMeshState(state)
        buffer.draw()
    }

    fun unload() {
        when (state) {
            MeshStates.LOADED -> buffer.unload()
            MeshStates.WAITING, MeshStates.PRELOADED -> _data?.nullCast<DirectArrayFloatList>()?.unload()
            MeshStates.UNLOADED -> throw InvalidMeshState(state)
        }
        state = MeshStates.UNLOADED
    }


    inline fun addXQuad(start: Vec2, x: Float, end: Vec2, uvStart: Vec2 = Vec2Util.EMPTY, uvEnd: Vec2 = Vec2Util.ONE, vertexConsumer: (position: Vec3, uv: Vec2) -> Unit) {
        val positions = arrayOf(
            Vec3(x, start.x, start.y),
            Vec3(x, start.x, end.y),
            Vec3(x, end.x, end.y),
            Vec3(x, end.x, start.y),
        )
        addQuad(positions, uvStart, uvEnd, vertexConsumer)
    }

    inline fun addYQuad(start: Vec2, y: Float, end: Vec2, uvStart: Vec2 = Vec2Util.EMPTY, uvEnd: Vec2 = Vec2Util.ONE, vertexConsumer: (position: Vec3, uv: Vec2) -> Unit) {
        val positions = arrayOf(
            Vec3(start.x, y, end.y),
            Vec3(end.x, y, end.y),
            Vec3(end.x, y, start.y),
            Vec3(start.x, y, start.y),
        )
        addQuad(positions, uvStart, uvEnd, vertexConsumer)
    }

    inline fun addZQuad(start: Vec2, z: Float, end: Vec2, uvStart: Vec2 = Vec2Util.EMPTY, uvEnd: Vec2 = Vec2Util.ONE, vertexConsumer: (position: Vec3, uv: Vec2) -> Unit) {
        val positions = arrayOf(
            Vec3(start.x, start.y, z),
            Vec3(start.x, end.y, z),
            Vec3(end.x, end.y, z),
            Vec3(end.x, start.y, z),
        )
        addQuad(positions, uvStart, uvEnd, vertexConsumer)
    }

    inline fun addQuad(positions: Array<Vec3>, uvStart: Vec2 = Vec2Util.EMPTY, uvEnd: Vec2 = Vec2Util.ONE, vertexConsumer: (position: Vec3, uv: Vec2) -> Unit) {
        val texturePositions = arrayOf(
            uvStart,
            Vec2(uvStart.x, uvEnd.y),
            uvEnd,
            Vec2(uvEnd.x, uvStart.y),
        )
        order.iterate { position, uv -> vertexConsumer.invoke(positions[position], texturePositions[uv]) }
    }

    override fun ensureSize(floats: Int) {
        data.ensureSize(floats)
    }

    enum class MeshStates {
        WAITING,
        PRELOADED,
        LOADED,
        UNLOADED,
        ;
    }

    class InvalidMeshState(state: MeshStates) : Exception("Invalid mesh state: $state")
}
