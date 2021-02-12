package de.bixilon.minosoft.gui.rendering.hud.elements.text

import de.bixilon.minosoft.gui.rendering.font.FontBindings

interface HUDText {

    fun prepare(chatComponents: Map<FontBindings, MutableList<Any>>)
    fun update() {}
    fun draw() {}
}
