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

package de.bixilon.minosoft.gui.rendering.system.opengl.buffer.render

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.system.base.buffer.render.Renderbuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.render.RenderbufferModes
import de.bixilon.minosoft.gui.rendering.system.base.buffer.render.RenderbufferStates
import de.bixilon.minosoft.gui.rendering.system.opengl.MemoryLeakException
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem
import org.lwjgl.opengl.GL30.*

class OpenGLRenderbuffer(
    private val renderSystem: OpenGLRenderSystem,
    override val mode: RenderbufferModes,
    override val size: Vec2i,
) : Renderbuffer {
    override var state: RenderbufferStates = RenderbufferStates.PREPARING
        private set
    var id = -1
        private set

    override fun init() {
        check(state == RenderbufferStates.PREPARING) { "Can not init renderbuffer in $state" }
        id = glGenRenderbuffers()
        unsafeBind()
        glRenderbufferStorage(GL_RENDERBUFFER, mode.gl, size.x, size.y)
        state = RenderbufferStates.GENERATED
    }

    fun bind() {
        check(state == RenderbufferStates.GENERATED) { "Can not bind renderbuffer in $state" }
        unsafeBind()
    }

    fun unsafeBind() {
        glBindRenderbuffer(GL_RENDERBUFFER, id)
    }

    override fun unload() {
        check(state == RenderbufferStates.GENERATED) { "Can not unload renderbuffer in $state" }
        glDeleteRenderbuffers(id)
        id = -1
        state = RenderbufferStates.UNLOADED
    }

    protected fun finalize() {
        if (state == RenderbufferStates.GENERATED && renderSystem.active) {
            throw MemoryLeakException("Renderbuffer has not been unloaded!")
        }
    }

    companion object {
        val RenderbufferModes.gl: Int
            get() {
                return when (this) {
                    RenderbufferModes.DEPTH_COMPONENT16 -> GL_DEPTH_COMPONENT16
                    RenderbufferModes.DEPTH_COMPONENT24 -> GL_DEPTH_COMPONENT24
                    RenderbufferModes.DEPTH_COMPONENT32F -> GL_DEPTH_COMPONENT32
                    RenderbufferModes.DEPTH_24_STENCIL_8 -> GL_DEPTH24_STENCIL8
                    RenderbufferModes.DEPTH_32F_STENCIL_8 -> GL_DEPTH32F_STENCIL8
                    RenderbufferModes.STENCIL_INDEX8 -> GL_STENCIL_INDEX8
                    else -> throw IllegalArgumentException("OpenGL does not support renderbuffer mode: $this")
                }
            }
    }
}
