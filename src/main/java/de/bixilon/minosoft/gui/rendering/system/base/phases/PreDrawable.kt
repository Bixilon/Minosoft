package de.bixilon.minosoft.gui.rendering.system.base.phases

import de.bixilon.minosoft.gui.rendering.renderer.Renderer

interface PreDrawable : Renderer {
    val skipPre: Boolean
        get() = false

    fun drawPre()
}
