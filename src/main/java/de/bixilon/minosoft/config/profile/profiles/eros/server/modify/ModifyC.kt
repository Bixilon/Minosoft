package de.bixilon.minosoft.config.profile.profiles.eros.server.modify

import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager.delegate

/**
 * Configuration for the add or edit server dialog
 */
class ModifyC {
    /**
     * Shows releases in the version select dropdown
     */
    var showReleases by delegate(true)

    /**
     * Shows snapshots in the version select dropdown
     */
    var showSnapshots by delegate(false)
}
