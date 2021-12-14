package de.bixilon.minosoft.gui.rendering.framebuffer

import de.bixilon.minosoft.gui.rendering.Drawable
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.framebuffer.defaultf.DefaultFramebuffer

class FramebufferManager(
    private val renderWindow: RenderWindow,
) : Drawable {
    val default = DefaultFramebuffer(renderWindow)

    // val gui = GUIFramebuffer(renderWindow)


    fun init() {
        default.init()
        //    gui = renderWindow.renderSystem.createFramebuffer(2)
    }


    override fun draw() {
        default.draw()
    }
}
