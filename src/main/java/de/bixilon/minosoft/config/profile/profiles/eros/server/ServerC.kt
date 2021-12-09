package de.bixilon.minosoft.config.profile.profiles.eros.server

import com.fasterxml.jackson.annotation.JsonInclude
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.listDelegate
import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.Server
import de.bixilon.minosoft.config.profile.profiles.eros.server.list.ListC
import de.bixilon.minosoft.config.profile.profiles.eros.server.modify.ModifyC

class ServerC {
    val modify = ModifyC()
    val list = ListC()

    @get:JsonInclude(JsonInclude.Include.NON_EMPTY)
    var entries: MutableList<Server> by listDelegate()
}
