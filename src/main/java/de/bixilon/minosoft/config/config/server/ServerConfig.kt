package de.bixilon.minosoft.config.config.server

import de.bixilon.minosoft.gui.main.Server

data class ServerConfig(
    val entries: MutableMap<Int, Server> = mutableMapOf(),
)
