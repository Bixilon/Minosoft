/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.util

import org.lwjgl.opengl.GL11.GL_TRIANGLES
import org.lwjgl.opengl.GL11.glDrawArrays
import org.lwjgl.opengl.GL30.*

abstract class Mesh {
    protected val data: MutableList<Float> = mutableListOf()
    private var vao: Int = -1
    private var vbo: Int = -1
    var trianglesCount: Int = -1
        private set
    private var rawData: FloatArray? = null

    var state = MeshStates.PREPARING
        private set

    open fun preLoad() {
        check(state == MeshStates.PREPARING) { "Mesh already loaded: $state" }
        rawData = data.toFloatArray()
        data.clear()
        state = MeshStates.PRE_LOADED
    }

    abstract fun load()

    protected fun initializeBuffers(floatsPerVertex: Int) {
        check(state == MeshStates.PRE_LOADED) { "Mesh not pre loaded: $state" }

        trianglesCount = rawData!!.size / floatsPerVertex
        vao = glGenVertexArrays()
        vbo = glGenBuffers()
        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)

        glBufferData(GL_ARRAY_BUFFER, rawData!!, GL_STATIC_DRAW)

        state = MeshStates.LOADED

        rawData = null
    }

    protected fun unbind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0)
    }

    fun draw() {
        // check(state == MeshStates.LOADED) { "Mesh not loaded: $state" }
        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLES, 0, trianglesCount)
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

    enum class MeshStates {
        PREPARING,
        PRE_LOADED,
        LOADED,
        UNLOADED,
    }
}
