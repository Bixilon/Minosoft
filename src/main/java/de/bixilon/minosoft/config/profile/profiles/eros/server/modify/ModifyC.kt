package de.bixilon.minosoft.config.profile.profiles.eros.server.modify

import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.delegate

class ModifyC {
    var showReleases by delegate(true)
    var showSnapshots by delegate(false)
}
