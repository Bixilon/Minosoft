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

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.FloatVertexBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.util.collections.floats.DirectArrayFloatList
import glm_.vec2.Vec2
import glm_.vec3.Vec3

abstract class Mesh(
    val renderWindow: RenderWindow,
    private val struct: MeshStruct,
    private val primitiveType: PrimitiveTypes = renderWindow.renderSystem.preferredPrimitiveType,
    initialCacheSize: Int = 10000,
    val clearOnLoad: Boolean = true,
    data: DirectArrayFloatList? = null,
) {
    val order = renderWindow.renderSystem.primitiveMeshOrder
    val reversedOrder = order.reversedArray()
    private var _data: DirectArrayFloatList? = data ?: DirectArrayFloatList(initialCacheSize)
    var data: DirectArrayFloatList
        get() = _data as DirectArrayFloatList
        set(value) {
            _data = value
        }

    protected lateinit var buffer: FloatVertexBuffer

    var vertices: Int = -1
        protected set

    var state = MeshStates.PREPARING
        protected set


    fun load() {
        buffer = renderWindow.renderSystem.createVertexBuffer(struct, data.buffer, primitiveType)
        buffer.init()
        if (clearOnLoad) {
            data.unload()
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
            Vec3(start.x, y, start.y),
            Vec3(start.x, y, end.y),
            Vec3(end.x, y, end.y),
            Vec3(end.x, y, start.y),
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

    private fun addQuad(positions: Array<Vec3>, uvStart: Vec2 = Vec2(0.0f, 0.0f), uvEnd: Vec2 = Vec2(1.0f, 1.0f), vertexConsumer: (position: Vec3, uv: Vec2) -> Unit) {
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
