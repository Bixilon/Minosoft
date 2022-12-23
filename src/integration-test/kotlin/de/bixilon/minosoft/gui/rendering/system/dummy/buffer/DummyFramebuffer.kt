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

package de.bixilon.minosoft.gui.rendering.system.dummy.buffer

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.FramebufferState
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.texture.FramebufferTexture
import de.bixilon.minosoft.gui.rendering.system.base.buffer.render.Renderbuffer

class DummyFramebuffer : Framebuffer {
    override val state: FramebufferState = FramebufferState.COMPLETE

    override fun init() {
    }

    override fun delete() {
    }

    override fun attach(renderbuffer: Renderbuffer) {
    }

    override fun attach(texture: FramebufferTexture) {
    }

    override fun bindTexture() {
    }

    override fun resize(size: Vec2i) {
    }
}
