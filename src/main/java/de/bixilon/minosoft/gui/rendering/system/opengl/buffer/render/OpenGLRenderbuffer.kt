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

import de.bixilon.minosoft.gui.rendering.system.base.buffer.render.Renderbuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.render.RenderbufferModes
import glm_.vec2.Vec2i
import org.lwjgl.opengl.GL30.*

class OpenGLRenderbuffer(
    override val mode: RenderbufferModes,
    override val size: Vec2i,
) : Renderbuffer {
    var id = -1
        private set

    override fun init() {
        id = glGenRenderbuffers()
        bind()
        glRenderbufferStorage(GL_RENDERBUFFER, mode.gl, size.x, size.y)
    }

    fun bind() {
        glBindRenderbuffer(GL_RENDERBUFFER, id)
    }

    override fun unload() {
        glDeleteRenderbuffers(id)
        id = -1
    }

    companion object {
        private val RenderbufferModes.gl: Int
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
