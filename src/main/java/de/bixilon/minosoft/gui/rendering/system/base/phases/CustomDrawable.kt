package de.bixilon.minosoft.gui.rendering.system.base.phases

import de.bixilon.minosoft.gui.rendering.renderer.Renderer

interface CustomDrawable : Renderer {
    val skipCustom: Boolean
        get() = false

    fun drawCustom()
}
