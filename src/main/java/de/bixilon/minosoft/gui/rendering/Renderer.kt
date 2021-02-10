package de.bixilon.minosoft.gui.rendering

import de.bixilon.minosoft.protocol.network.Connection

interface Renderer {

    fun init(connection: Connection)

    fun draw()
}
