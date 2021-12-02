package de.bixilon.minosoft.config.profile.util.delegate

import de.bixilon.minosoft.config.profile.ProfileManager

open class ProfileDelegate<V>(
    private var value: V,
    checkEquals: Boolean,
    profileManager: ProfileManager<*>,
    profileName: String,
    verify: ((V) -> Unit)?,
) : BackingDelegate<V>(checkEquals, profileManager, profileName, verify) {

    override fun get() = value
    override fun set(value: V) {
        this.value = value
    }
}
