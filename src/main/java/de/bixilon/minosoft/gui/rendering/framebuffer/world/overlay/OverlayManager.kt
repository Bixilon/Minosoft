package de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.overlays.DefaultOverlays
import de.bixilon.minosoft.gui.rendering.renderer.Drawable

class OverlayManager(
    private val renderWindow: RenderWindow,
) : Drawable {
    private val overlays: MutableList<Overlay> = mutableListOf()

    fun init() {
        for ((index, factory) in DefaultOverlays.OVERLAYS.withIndex()) {
            overlays += factory.build(renderWindow, WORLD_FRAMEBUFFER_Z + (-0.01f * (index + 1)))
        }

        for (overlay in overlays) {
            overlay.init()
        }
    }

    fun postInit() {
        for (overlay in overlays) {
            overlay.postInit()
        }
    }

    override fun draw() {
        for (overlay in overlays) {
            if (!overlay.render) {
                continue
            }
            overlay.draw()
        }
    }

    companion object {
        private const val WORLD_FRAMEBUFFER_Z = -0.1f
    }
}
