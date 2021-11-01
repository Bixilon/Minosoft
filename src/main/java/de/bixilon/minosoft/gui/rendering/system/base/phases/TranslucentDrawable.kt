package de.bixilon.minosoft.gui.rendering.system.base.phases

import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.system.base.BlendingFunctions

interface TranslucentDrawable : Renderer {

    fun setupTranslucent() {
        renderSystem.reset(depthMask = false) // ToDo: This is just a translucent workaround
        renderSystem.setBlendFunc(BlendingFunctions.SOURCE_ALPHA, BlendingFunctions.ONE_MINUS_SOURCE_ALPHA, BlendingFunctions.ONE, BlendingFunctions.ONE_MINUS_SOURCE_ALPHA)
    }

    fun drawTranslucent()
}
