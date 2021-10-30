package de.bixilon.minosoft.gui.rendering.gui.elements

interface Pollable {

    /**
     * A generic function that gets called in a specific interval to check for changed data
     * @return true, if any value changed
     */
    fun poll(): Boolean
}
