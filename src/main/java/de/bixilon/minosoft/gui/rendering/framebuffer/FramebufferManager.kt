package de.bixilon.minosoft.gui.rendering.framebuffer

import de.bixilon.minosoft.gui.rendering.Drawable
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.framebuffer.defaultf.DefaultFramebuffer
import de.bixilon.minosoft.gui.rendering.framebuffer.gui.GUIFramebuffer
import de.bixilon.minosoft.gui.rendering.modding.events.ResizeWindowEvent
import de.bixilon.minosoft.gui.rendering.system.base.IntegratedBufferTypes
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker

class FramebufferManager(
    private val renderWindow: RenderWindow,
) : Drawable {
    val default = DefaultFramebuffer(renderWindow)
    val gui = GUIFramebuffer(renderWindow)


    fun init() {
        default.init()
        gui.init()

        renderWindow.connection.registerEvent(CallbackEventInvoker.of<ResizeWindowEvent> {
            default.framebuffer.resize(it.size)
            gui.framebuffer.resize(it.size)
        })
    }


    fun clear() {
        default.clear()
        gui.clear()
    }


    override fun draw() {
        default.draw()
        renderWindow.renderSystem.clear(IntegratedBufferTypes.DEPTH_BUFFER, IntegratedBufferTypes.STENCIL_BUFFER)
        gui.draw()
    }
}
