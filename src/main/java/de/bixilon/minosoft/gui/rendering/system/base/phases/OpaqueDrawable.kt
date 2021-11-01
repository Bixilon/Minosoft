package de.bixilon.minosoft.gui.rendering.system.base.phases

import de.bixilon.minosoft.gui.rendering.Renderer

interface OpaqueDrawable : Renderer {

    fun setupOpaque() {
        renderSystem.reset()
    }

    fun drawOpaque()
}
