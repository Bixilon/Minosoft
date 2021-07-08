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

import de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex.PrimitiveTypes
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.vertex.FloatOpenGLVertexBuffer
import de.bixilon.minosoft.util.collections.ArrayFloatList
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import kotlin.reflect.KClass

abstract class Mesh(
    private val struct: KClass<*>,
    private val primitiveType: PrimitiveTypes = PrimitiveTypes.TRIANGLE,
    initialCacheSize: Int = 10000,
) {
    private var _data: ArrayFloatList? = ArrayFloatList(initialCacheSize)
    var data: ArrayFloatList
        get() = _data!!
        set(value) {
            _data = value
        }

    protected lateinit var buffer: FloatOpenGLVertexBuffer

    var vertices: Int = -1
        protected set

    var state = MeshStates.PREPARING
        protected set


    fun load() {
        buffer = FloatOpenGLVertexBuffer(struct, data.toArray(), primitiveType)
        buffer.init()
        vertices = buffer.vertices
    }

    fun draw() {
        buffer.draw()
    }

    fun unload(todo: Boolean = false) {
        buffer.unload()
    }

    fun addQuad(start: Vec3, end: Vec3, textureStart: Vec2 = Vec2(0.0f, 0.0f), textureEnd: Vec2 = Vec2(1.0f, 1.0f), vertexConsumer: (position: Vec3, textureCoordinate: Vec2) -> Unit) {
        val positions = arrayOf(
            start,
            Vec3(start.x, start.y, end.z),
            end,
            Vec3(end.x, end.y, start.z),
        )
        val texturePositions = arrayOf(
            Vec2(textureEnd.x, textureStart.y),
            textureStart,
            Vec2(textureStart.x, textureEnd.y),
            textureEnd,
        )

        for ((vertexIndex, textureIndex) in QUAD_DRAW_ODER) {
            vertexConsumer.invoke(positions[vertexIndex], texturePositions[textureIndex])
        }
    }

    enum class MeshStates {
        PREPARING,
        LOADED,
        UNLOADED,
    }

    companion object {
        val QUAD_DRAW_ODER = arrayOf(
            0 to 1,
            3 to 2,
            2 to 3,
            2 to 3,
            1 to 0,
            0 to 1,
        )
    }
}
