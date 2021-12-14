package de.bixilon.minosoft.gui.rendering.framebuffer

import de.bixilon.minosoft.gui.rendering.Drawable
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.framebuffer.defaultf.DefaultFramebuffer
import de.bixilon.minosoft.gui.rendering.modding.events.ResizeWindowEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker

class FramebufferManager(
    private val renderWindow: RenderWindow,
) : Drawable {
    val default = DefaultFramebuffer(renderWindow)


    fun init() {
        default.init()

        renderWindow.connection.registerEvent(CallbackEventInvoker.of<ResizeWindowEvent> {
            default.framebuffer.resize(it.size)
        })
    }


    override fun draw() {
        default.draw()
    }
}
