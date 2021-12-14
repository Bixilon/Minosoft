package de.bixilon.minosoft.gui.rendering.system.base.buffer.frame.texture

import glm_.vec2.Vec2i

interface FramebufferTexture {
    val size: Vec2i
    // ToDo: Mode

    fun init()
    fun unload()
}
