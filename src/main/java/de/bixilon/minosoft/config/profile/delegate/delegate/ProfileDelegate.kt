package de.bixilon.minosoft.config.profile.delegate.delegate

import de.bixilon.minosoft.config.profile.ProfileManager

open class ProfileDelegate<V>(
    private var value: V,
    profileManager: ProfileManager<*>,
    profileName: String,
    verify: ((V) -> Unit)?,
) : BackingDelegate<V>(profileManager, profileName, verify) {

    override fun get() = value
    override fun set(value: V) {
        this.value = value
    }
}
