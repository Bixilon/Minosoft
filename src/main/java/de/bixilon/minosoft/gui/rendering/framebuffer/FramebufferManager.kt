package de.bixilon.minosoft.gui.rendering.framebuffer

import de.bixilon.minosoft.gui.rendering.Drawable
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.framebuffer.gui.GUIFramebuffer
import de.bixilon.minosoft.gui.rendering.framebuffer.world.WorldFramebuffer
import de.bixilon.minosoft.gui.rendering.modding.events.ResizeWindowEvent
import de.bixilon.minosoft.modding.event.invoker.CallbackEventInvoker

class FramebufferManager(
    private val renderWindow: RenderWindow,
) : Drawable {
    val world = WorldFramebuffer(renderWindow)
    val gui = GUIFramebuffer(renderWindow)


    fun init() {
        world.init()
        gui.init()

        renderWindow.connection.registerEvent(CallbackEventInvoker.of<ResizeWindowEvent> {
            world.framebuffer.resize(it.size)
            gui.framebuffer.resize(it.size)
        })
    }


    fun clear() {
        world.clear()
        gui.clear()
    }


    override fun draw() {
        world.draw()
        gui.draw()
    }
}
