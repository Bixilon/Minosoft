package de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay

import de.bixilon.minosoft.gui.rendering.renderer.Drawable

interface Overlay : Drawable {
    val render: Boolean

    fun init() {}
    fun postInit() {}
}
