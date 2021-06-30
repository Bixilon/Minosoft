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

package de.bixilon.minosoft.gui.rendering.system.opengl

import de.bixilon.minosoft.gui.rendering.shader.Shader
import org.lwjgl.opengl.ARBUniformBufferObject.*
import org.lwjgl.opengl.GL15.glBindBuffer
import org.lwjgl.opengl.GL15.glGenBuffers

abstract class UniformBuffer(private val bindingIndex: Int) {
    private var id = -1
    protected abstract val size: Int
    protected var initialSize = -1


    fun init() {
        id = glGenBuffers()
        initialUpload()
        glBindBufferRange(GL_UNIFORM_BUFFER, bindingIndex, id, 0, size.toLong())
        initialSize = size
    }

    protected fun bind() {
        glBindBuffer(GL_UNIFORM_BUFFER, id)
    }

    protected fun unbind() {
        glBindBuffer(GL_UNIFORM_BUFFER, 0)
    }


    fun use(shader: Shader, bufferName: String) {
        shader.use()

        shader.setUniformBuffer(bufferName, bindingIndex)
        glBindBufferBase(GL_UNIFORM_BUFFER, bindingIndex, id)
    }

    protected abstract fun initialUpload()

    abstract fun upload()
}
