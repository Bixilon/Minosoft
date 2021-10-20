package de.bixilon.minosoft.gui.rendering.gui.elements

interface Pollable {

    /**
     * @return true, if any value changed
     */
    fun poll(): Boolean
}
