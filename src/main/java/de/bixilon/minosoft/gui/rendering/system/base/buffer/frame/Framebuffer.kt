package de.bixilon.minosoft.gui.rendering.system.base.buffer.frame

import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.texture.FramebufferTexture
import de.bixilon.minosoft.gui.rendering.system.base.buffer.render.Renderbuffer

interface Framebuffer {
    val state: FramebufferState

    fun init()
    fun delete()

    fun attach(renderbuffer: Renderbuffer)
    fun attach(texture: FramebufferTexture)

    fun bindTexture()
}
