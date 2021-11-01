package de.bixilon.minosoft.gui.rendering.system.base.phases

import de.bixilon.minosoft.gui.rendering.Renderer

interface TransparentDrawable : Renderer {

    fun setupTransparent() {
        renderSystem.reset(blending = false)
    }

    fun drawTransparent()
}
