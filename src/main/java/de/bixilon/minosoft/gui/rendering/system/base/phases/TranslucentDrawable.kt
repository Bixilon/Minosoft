package de.bixilon.minosoft.gui.rendering.system.base.phases

import de.bixilon.minosoft.gui.rendering.Renderer

interface TranslucentDrawable : Renderer {
    val skipTranslucent: Boolean
        get() = false

    fun setupTranslucent() {
        renderSystem.reset(blending = true)
    }

    fun drawTranslucent()
}
