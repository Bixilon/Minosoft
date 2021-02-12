package de.bixilon.minosoft.gui.rendering

interface Renderer {
    fun init()
    fun draw()
    fun screenChangeResizeCallback(width: Int, height: Int) {}
}
