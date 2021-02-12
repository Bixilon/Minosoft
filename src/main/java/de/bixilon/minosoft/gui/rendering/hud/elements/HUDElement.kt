package de.bixilon.minosoft.gui.rendering.hud.elements

interface HUDElement {

    fun init()
    fun prepare()
    fun update()
    fun draw()
    fun screenChangeResizeCallback(width: Int, height: Int) {}
}
