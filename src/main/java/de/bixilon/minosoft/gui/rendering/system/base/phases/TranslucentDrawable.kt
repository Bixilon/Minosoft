package de.bixilon.minosoft.gui.rendering.system.base.phases

import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions

interface TranslucentDrawable : Renderer {
    val skipTranslucent: Boolean
        get() = false

    fun setupTranslucent() {
        renderSystem.reset(sourceAlpha = BlendingFunctions.SOURCE_ALPHA, destinationAlpha = BlendingFunctions.ONE_MINUS_SOURCE_ALPHA, blending = true)
    }

    fun drawTranslucent()
}
