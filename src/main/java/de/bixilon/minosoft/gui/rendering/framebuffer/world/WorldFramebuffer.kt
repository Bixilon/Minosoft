package de.bixilon.minosoft.gui.rendering.framebuffer.world

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.framebuffer.FramebufferMesh
import de.bixilon.minosoft.gui.rendering.framebuffer.IntegratedFramebuffer
import de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.Framebuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class WorldFramebuffer(
    override val renderWindow: RenderWindow,
) : IntegratedFramebuffer {
    override val shader = renderWindow.renderSystem.createShader("minosoft:framebuffer/world".toResourceLocation())
    override val framebuffer: Framebuffer = renderWindow.renderSystem.createFramebuffer()
    override val mesh = FramebufferMesh(renderWindow)
}
