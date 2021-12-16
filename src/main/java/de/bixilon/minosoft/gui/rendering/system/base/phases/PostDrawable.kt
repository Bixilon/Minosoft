package de.bixilon.minosoft.gui.rendering.system.base.phases

import de.bixilon.minosoft.gui.rendering.Renderer

interface PostDrawable : Renderer {
    val skipPost: Boolean
        get() = false

    fun drawPost()
}
