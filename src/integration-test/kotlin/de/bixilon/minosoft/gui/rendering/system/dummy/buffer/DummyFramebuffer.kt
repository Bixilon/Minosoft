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

package de.bixilon.minosoft.gui.rendering.system.dummy.buffer

import de.bixilon.kmath.vec.vec2.i.Vec2i
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.FramebufferState
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.depth.DepthAttachment
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.stencil.StencilAttachment
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.attachment.texture.TextureAttachment

class DummyFramebuffer(
    override val size: Vec2i = Vec2i(1, 1),
    override val scale: Float = 1.0f,
) : Framebuffer {
    override val state: FramebufferState = FramebufferState.COMPLETE

    override val depth: DepthAttachment? get() = null
    override val stencil: StencilAttachment? get() = null
    override val texture: TextureAttachment? get() = null

    override fun init() {
    }

    override fun delete() {
    }

    override fun bindTexture() {
    }
}
