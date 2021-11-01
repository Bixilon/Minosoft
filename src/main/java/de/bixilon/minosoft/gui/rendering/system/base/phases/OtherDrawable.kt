package de.bixilon.minosoft.gui.rendering.system.base.phases

import de.bixilon.minosoft.gui.rendering.Renderer

interface OtherDrawable : Renderer {

    fun setupOther() {
        renderSystem.reset()
    }

    fun drawOther()
}
