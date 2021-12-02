package de.bixilon.minosoft.config.profile.profiles.eros.server.list

import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.delegate

class ListC {
    /**
     * Hides all servers in the server list that can not be pinged
     */
    var hideOffline by delegate(false)

    /**
     * Hides all servers in the server list, when the amount of online players exceeds the slots
     */
    var hideFull by delegate(false)

    /**
     * Hides all servers in the server list when <= 0 players are online
     */
    var hideEmpty by delegate(false)
}
