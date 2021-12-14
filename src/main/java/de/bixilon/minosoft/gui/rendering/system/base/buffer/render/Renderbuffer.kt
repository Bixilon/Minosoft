package de.bixilon.minosoft.gui.rendering.system.base.buffer.render

import glm_.vec2.Vec2i

interface Renderbuffer {
    val mode: RenderbufferModes
    val size: Vec2i

    fun init()
}
