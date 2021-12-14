package de.bixilon.minosoft.gui.rendering.system.base.buffer.frame

import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.texture.FramebufferTexture
import de.bixilon.minosoft.gui.rendering.system.base.buffer.render.Renderbuffer
import glm_.vec2.Vec2i

interface Framebuffer {
    val state: FramebufferState

    fun init()
    fun delete()

    fun attach(renderbuffer: Renderbuffer)
    fun attach(texture: FramebufferTexture)

    fun bindTexture()

    fun resize(size: Vec2i)
}
