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

import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct.Companion.BYTES
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.Util
import de.bixilon.minosoft.util.collections.ArrayFloatList
import glm_.vec2.Vec2
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL11.GL_TRIANGLES
import org.lwjgl.opengl.GL11.glDrawArrays
import org.lwjgl.opengl.GL30.*
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.primaryConstructor

abstract class Mesh(
    protected val struct: KClass<*>,
    initialCacheSize: Int = 10000,
) {
    protected var _data: ArrayFloatList? = ArrayFloatList(initialCacheSize)
    var data: ArrayFloatList
        get() = _data!!
        set(value) {
            _data = value
        }

    protected var vao: Int = -1
    protected var vbo: Int = -1
    var primitiveCount: Int = -1
        protected set

    var state = MeshStates.PREPARING
        protected set


    open fun load() {
        Util.forceClassInit(struct.java)

        val bytesPerVertex = struct.companionObjectInstance!!.unsafeCast<MeshStruct>().BYTES_PER_VERTEX

        initializeBuffers(bytesPerVertex / Float.SIZE_BYTES)


        var stride = 0L

        for ((index, parameter) in struct.primaryConstructor!!.parameters.withIndex()) {
            val bytes = parameter.BYTES
            glVertexAttribPointer(index, bytes / Float.SIZE_BYTES, GL_FLOAT, false, bytesPerVertex, stride)
            glEnableVertexAttribArray(index)
            stride += bytes
        }
        unbind()
    }


    protected fun initializeBuffers(floatsPerVertex: Int) {
        check(state == MeshStates.PREPARING) { "Mesh already loaded: $state" }

        primitiveCount = data.size / floatsPerVertex
        vao = glGenVertexArrays()
        vbo = glGenBuffers()
        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)

        glBufferData(GL_ARRAY_BUFFER, data.toArray(), GL_STATIC_DRAW)

        state = MeshStates.LOADED
        _data = null
    }

    protected fun unbind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }

    open fun draw() {
        // check(state == MeshStates.LOADED) { "Mesh not loaded: $state" }
        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLES, 0, primitiveCount)
    }

    fun unload(checkLoaded: Boolean = true) {
        if (checkLoaded) {
            check(state == MeshStates.LOADED) { "Mesh not loaded: $state" }
        } else {
            if (state != MeshStates.LOADED) {
                return
            }
        }
        glDeleteVertexArrays(vao)
        glDeleteBuffers(vbo)
        state = MeshStates.UNLOADED
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
