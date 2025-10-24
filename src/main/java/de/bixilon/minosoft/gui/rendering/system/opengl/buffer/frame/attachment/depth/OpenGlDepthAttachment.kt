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

package de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame.attachment.depth

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.depth.DepthAttachment
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.depth.DepthModes
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame.attachment.OpenGlBufferAttachment
import de.bixilon.minosoft.gui.rendering.system.opengl.error.MemoryLeakException
import org.lwjgl.opengl.GL30.*

class OpenGlDepthAttachment(
    system: OpenGlRenderSystem,
    size: Vec2i,
    override val mode: DepthModes,
) : OpenGlBufferAttachment(system, size), DepthAttachment {

    override val glMode get() = mode.gl

    companion object {
        val DepthModes.gl: Int
            get() {
                return when (this) {
                    DepthModes.DEPTH8 -> GL_DEPTH_COMPONENT16
                    DepthModes.DEPTH24 -> GL_DEPTH_COMPONENT24
                    DepthModes.DEPTH32 -> GL_DEPTH_COMPONENT32
                    else -> throw IllegalArgumentException("OpenGL does not support depth mode: $this")
                }
            }
    }
}
