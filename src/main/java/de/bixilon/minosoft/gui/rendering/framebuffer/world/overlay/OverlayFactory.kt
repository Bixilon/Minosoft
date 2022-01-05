package de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay

import de.bixilon.minosoft.gui.rendering.RenderWindow

interface OverlayFactory<T : Overlay> {

    fun build(renderWindow: RenderWindow, z: Float): T
}
