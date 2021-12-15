package de.bixilon.minosoft.gui.rendering.framebuffer.gui

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.framebuffer.FramebufferMesh
import de.bixilon.minosoft.gui.rendering.framebuffer.IntegratedFramebuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class GUIFramebuffer(
    override val renderWindow: RenderWindow,
) : IntegratedFramebuffer {
    override val shader = renderWindow.renderSystem.createShader("minosoft:framebuffer/gui".toResourceLocation())
    override val framebuffer: Framebuffer = renderWindow.renderSystem.createFramebuffer()
    override val mesh = FramebufferMesh(renderWindow)
}
