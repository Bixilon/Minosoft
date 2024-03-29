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

import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferDrawTypes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferStates
import de.bixilon.minosoft.gui.rendering.system.base.buffer.RenderableBufferTypes
import de.bixilon.minosoft.gui.rendering.system.opengl.MemoryLeakException
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL31.GL_UNIFORM_BUFFER

abstract class OpenGLRenderableBuffer(
    protected var system: OpenGLRenderSystem,
    override val type: RenderableBufferTypes,
) : RenderableBuffer {
    override var state: RenderableBufferStates = RenderableBufferStates.PREPARING
    abstract val drawTypes: RenderableBufferDrawTypes

    protected var id: Int = -1
        private set

    override fun init() {
        system.log { "Generated renderable buffer $this" }
        id = glGenBuffers()
    }

    override fun bind() {
        if (system.boundBuffer == id) {
            return
        }
        glBindBuffer(type.gl, id)
        system.boundBuffer = id
    }

    override fun unbind() {
        if (RenderConstants.DIRTY_BUFFER_UNBIND) {
            // This is unclean, yes. But it is not required to do at all (we always bind another buffer), so this saves a ton of gl calls
            return
        }
        glBindBuffer(type.gl, 0)
    }

    override fun unload() {
        check(state == RenderableBufferStates.UPLOADED) { "Buffer is not uploaded: $state" }
        glDeleteBuffers(id)
        if (system.boundBuffer == id) {
            system.boundBuffer = -1
        }
        id = -1
        state = RenderableBufferStates.UNLOADED
    }

    protected fun finalize() {
        if (state == RenderableBufferStates.UPLOADED && system.active) {
            throw MemoryLeakException("Buffer has not been unloaded!")
        }
    }

    protected companion object {
        val RenderableBufferTypes.gl: Int
            get() {
                return when (this) {
                    RenderableBufferTypes.UNIFORM_BUFFER -> GL_UNIFORM_BUFFER
                    RenderableBufferTypes.ARRAY_BUFFER -> GL_ARRAY_BUFFER
                    else -> throw IllegalArgumentException("OpenGL does not support buffer type: $this")
                }
            }

        val RenderableBufferDrawTypes.gl: Int
            get() {
                return when (this) {
                    RenderableBufferDrawTypes.DYNAMIC -> GL_DYNAMIC_DRAW
                    RenderableBufferDrawTypes.STATIC -> GL_STATIC_DRAW
                    else -> throw IllegalArgumentException("OpenGL does not support buffer draw type: $this")
                }
            }
    }
}
