/*
 * Minosoft
 * Copyright (C) 2020-2026 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame.attachment.texture

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.AttachmentStates
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.texture.TextureAttachment
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.texture.TextureModes
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem
import de.bixilon.minosoft.gui.rendering.system.opengl.OpenGlRenderSystem.Companion.gl
import de.bixilon.minosoft.gui.rendering.system.opengl.buffer.frame.attachment.OpenGlFramebufferAttachment
import org.lwjgl.opengl.GL30.*
import java.nio.ByteBuffer

class OpenGlTextureAttachment(
    system: OpenGlRenderSystem,
    size: Vec2i,
    override val mode: TextureModes,
) : OpenGlFramebufferAttachment(system, size), TextureAttachment {
    var id = -1

    override fun init() {
        if (state != AttachmentStates.PREPARING) throw IllegalStateException("Already initialized (state=$state)")

        id = gl { glGenTextures() }
        gl { glActiveTexture(GL_TEXTURE0 + system.framebufferTextureIndex) }
        gl { glBindTexture(GL_TEXTURE_2D, id) }
        system.boundTexture = id
        gl { glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, size.x, size.y, 0, GL_RGBA, GL_UNSIGNED_BYTE, null as ByteBuffer?) }
        gl { glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST) }
        gl { glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST) }

        state = AttachmentStates.GENERATED
    }


    fun bind() {
        if (state != AttachmentStates.GENERATED) throw IllegalStateException("Not loaded (state=$state)")


        if (system.boundTexture != id) {
            gl { glActiveTexture(GL_TEXTURE0 + system.framebufferTextureIndex) }
            gl { glBindTexture(GL_TEXTURE_2D, id) }
            system.boundTexture = id
        }
    }

    override fun unload() {
        if (state != AttachmentStates.GENERATED) throw IllegalStateException("Not loaded (state=$state)")
        gl { glDeleteTextures(id) }
        id = -1
        state = AttachmentStates.UNLOADED
    }
}
