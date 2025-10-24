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

import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGLRenderSystem.Companion.gl
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture

abstract class OpenGLTexture {
    var state = OpenGLTextureStates.PREPARING
        protected set
    var id: Int = -1
        protected set

    abstract fun init()

    fun bind(target: Int) {
        if (state != OpenGLTextureStates.INITIALIZED) throw IllegalStateException("Not loaded (state=$state)")
        check(target in 0 until 12)
        gl { glActiveTexture(GL_TEXTURE0 + target) }
        gl { glBindTexture(GL_TEXTURE_2D, id) }
    }

    fun unload() {
        if (state != OpenGLTextureStates.INITIALIZED) throw IllegalStateException("Not loaded (state=$state)")
        gl { glDeleteTextures(id) }
        id = -1
    }
}
