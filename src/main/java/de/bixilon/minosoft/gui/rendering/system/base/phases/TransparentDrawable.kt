package de.bixilon.minosoft.gui.rendering.system.base.phases

import de.bixilon.minosoft.gui.rendering.renderer.Renderer

interface TransparentDrawable : Renderer {
    val skipTransparent: Boolean
        get() = false

    fun setupTransparent() {
        renderSystem.reset()
    }

    fun drawTransparent()
}
