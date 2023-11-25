/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.system.opengl.buffer

import de.bixilon.minosoft.config.DebugOptions.EMPTY_BUFFERS
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderFloatBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferDrawTypes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferStates
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferTypes
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem
import org.lwjgl.opengl.GL15.glBufferSubData
import org.lwjgl.opengl.GL15C
import org.lwjgl.system.MemoryUtil.memAddress
import java.nio.FloatBuffer

open class FloatOpenGLBuffer(renderSystem: OpenGLRenderSystem, protected var _data: FloatBuffer?) : OpenGLRenderableBuffer(renderSystem, RenderableBufferTypes.ARRAY_BUFFER), RenderFloatBuffer {
    override var buffer: FloatBuffer
        get() = _data!!
        set(value) {
            _data = value
        }
    override val drawTypes: RenderableBufferDrawTypes = RenderableBufferDrawTypes.STATIC

    override fun initialUpload() {
        bind()
        val position = buffer.position()
        buffer.position(0)
        nglBufferData(type.gl, buffer, if (EMPTY_BUFFERS) 0 else position, drawTypes.gl)
        buffer.position(position)
        unbind()
        state = RenderableBufferStates.UPLOADED
    }

    override fun upload() {
        bind()
        glBufferSubData(type.gl, 0, buffer)
        unbind()
    }

    private fun nglBufferData(target: Int, buffer: FloatBuffer, length: Int, usage: Int) {
        GL15C.nglBufferData(target, Integer.toUnsignedLong(length) shl 2, memAddress(buffer), usage)
    }
}
