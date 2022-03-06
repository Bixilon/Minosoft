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

package de.bixilon.minosoft.gui.rendering.system.opengl.buffer.uniform

import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferStates
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.FloatUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem
import org.lwjgl.opengl.GL15.glBufferData
import org.lwjgl.opengl.GL15.glBufferSubData
import org.lwjgl.opengl.GL15C.nglBufferSubData
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.memAllocFloat
import java.nio.FloatBuffer

class FloatOpenGLUniformBuffer(renderSystem: OpenGLRenderSystem, bindingIndex: Int = 0, override var buffer: FloatBuffer = memAllocFloat(0)) : OpenGLUniformBuffer(renderSystem, bindingIndex), FloatUniformBuffer {
    override val size: Int
        get() = buffer.limit()

    override fun initialUpload() {
        bind()
        buffer.position(0)
        glBufferData(type.gl, buffer, drawTypes.gl)
        unbind()
        state = RenderableBufferStates.UPLOADED
    }

    override fun upload() {
        check(initialSize == size) { "Can not change buffer size!" }
        bind()
        glBufferSubData(type.gl, 0, buffer)
        unbind()
    }

    override fun upload(range: IntRange) {
        check(initialSize == size) { "Can not change buffer size!" }
        if (range.first < 0 || range.last >= size) {
            throw IndexOutOfBoundsException(range.first)
        }
        bind()
        nglBufferSubData(type.gl, range.first * 4L, Integer.toUnsignedLong(((range.last + 1) - range.first) * 4), MemoryUtil.memAddress(buffer, range.first))
        unbind()
    }
}
