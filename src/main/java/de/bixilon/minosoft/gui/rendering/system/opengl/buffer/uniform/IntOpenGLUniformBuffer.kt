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

package de.bixilon.minosoft.gui.rendering.system.opengl.buffer.uniform

import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferStates
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.IntUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem.Companion.gl
import org.lwjgl.opengl.GL15.glBufferData
import org.lwjgl.opengl.GL15.glBufferSubData

class IntOpenGLUniformBuffer(renderSystem: OpenGLRenderSystem, bindingIndex: Int = 0, override var data: IntArray = IntArray(0)) : OpenGLUniformBuffer(renderSystem, bindingIndex), IntUniformBuffer {
    override val size: Int
        get() = data.size

    override fun initialUpload() {
        bind()
        gl { glBufferData(type.gl, data, drawTypes.gl) }
        state = RenderableBufferStates.UPLOADED
        unbind()
    }

    override fun upload() {
        check(initialSize == size) { "Can not change buffer size!" }
        bind()
        gl { glBufferSubData(type.gl, 0, data) }
        unbind()
    }

    override fun upload(start: Int, end: Int) = TODO("Unsupported")
}
