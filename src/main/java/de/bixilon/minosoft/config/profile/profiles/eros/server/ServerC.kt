package de.bixilon.minosoft.config.profile.profiles.eros.server

import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.listDelegate
import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.Server
import de.bixilon.minosoft.config.profile.profiles.eros.server.list.ListC
import de.bixilon.minosoft.config.profile.profiles.eros.server.modify.ModifyC

class ServerC {
    val modify = ModifyC()
    val list = ListC()

    var entries: MutableList<Server> by listDelegate()
}
