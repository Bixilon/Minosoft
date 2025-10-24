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

package de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame.texture

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.texture.FramebufferTexture
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem.Companion.gl
import org.lwjgl.opengl.GL30.*
import java.nio.ByteBuffer

class OpenGLFramebufferColorTexture(
    override val size: Vec2i,
) : OpenGLTexture(), FramebufferTexture {

    override fun init() {
        if (state != OpenGLTextureStates.PREPARING) throw IllegalStateException("Already initialized (state=$state)")

        id = gl { glGenTextures() }
        gl { glBindTexture(GL_TEXTURE_2D, id) }
        gl { glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, size.x, size.y, 0, GL_RGBA, GL_UNSIGNED_BYTE, null as ByteBuffer?) }
        gl { glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST) }
        gl { glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST) }

        state = OpenGLTextureStates.INITIALIZED
    }
}
