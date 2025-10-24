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

import de.bixilon.minosoft.gui.rendering.system.base.buffer.GpuBufferStates
import de.bixilon.minosoft.gui.rendering.system.base.buffer.uniform.FloatUniformBuffer
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL15C.nglBufferSubData
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.memAllocFloat
import java.nio.FloatBuffer

class FloatOpenGlUniformBuffer(renderSystem: OpenGlRenderSystem, bindingIndex: Int = 0, override var buffer: FloatBuffer = memAllocFloat(0)) : OpenGlUniformBuffer(renderSystem, bindingIndex), FloatUniformBuffer {
    override val size: Int
        get() = buffer.limit()

    override fun initialUpload() {
        bind()
        buffer.position(0)
        gl { glBufferData(glType, buffer, GL_DYNAMIC_DRAW) }
        state = GpuBufferStates.UPLOADED
        unbind()
    }

    override fun upload() {
        check(initialSize == size) { "Can not change buffer size!" }
        bind()
        gl { glBufferSubData(glType, 0, buffer) }
        unbind()
    }

    override fun upload(start: Int, end: Int) {
        check(initialSize == size) { "Can not change buffer size!" }
        if (start < 0 || end >= size) {
            throw IndexOutOfBoundsException(start)
        }
        bind()
        gl { nglBufferSubData(glType, start * 4L, Integer.toUnsignedLong(((end + 1) - start) * 4), MemoryUtil.memAddress(buffer, start)) }
        unbind()
    }
}
