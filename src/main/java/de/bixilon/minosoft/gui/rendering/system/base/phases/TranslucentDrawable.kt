package de.bixilon.minosoft.gui.rendering.system.base.phases

import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions

interface TranslucentDrawable : Renderer {
    val skipTranslucent: Boolean
        get() = false

    fun setupTranslucent() {
        renderSystem.reset(blending = true)
        renderSystem.setBlendFunc(BlendingFunctions.SOURCE_ALPHA, BlendingFunctions.ONE_MINUS_SOURCE_ALPHA, BlendingFunctions.ONE, BlendingFunctions.ZERO)
    }

    fun drawTranslucent()
}
