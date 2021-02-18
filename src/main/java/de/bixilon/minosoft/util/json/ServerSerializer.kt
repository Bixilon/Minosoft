package de.bixilon.minosoft.util.json

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import de.bixilon.minosoft.gui.main.Server

class ServerSerializer {
    @FromJson
    fun fromJson(json: Map<String, Any>): Server {
        return Server.deserialize(json)
    }

    @ToJson
    fun toJson(server: Server): Map<String, Any> {
        return server.serialize()
    }
}
