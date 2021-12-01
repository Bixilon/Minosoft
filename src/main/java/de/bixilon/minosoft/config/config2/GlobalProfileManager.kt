package de.bixilon.minosoft.config.config2

import de.bixilon.minosoft.config.config2.config.eros.ErosProfileManager

object GlobalProfileManager {
    val DEFAULT_MANAGERS: List<ProfileManager<*>> = listOf(
        ErosProfileManager,
    )

    fun load() {
        for (manager in DEFAULT_MANAGERS) {
            manager.load(null)
        }
    }
}
