package de.bixilon.minosoft.config.profile.util.delegate

import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.change.ProfilesChangeManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

abstract class BackingDelegate<V>(
    private val checkEquals: Boolean,
    private val profileManager: ProfileManager<*>,
    private val profileName: String,
    private val verify: ((V) -> Unit)?,
) : ReadWriteProperty<Any, V>, DelegateSetter<V> {
    private lateinit var profile: Profile

    override fun getValue(thisRef: Any, property: KProperty<*>): V {
        return get()
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: V) {
        val previous = get()
        if (checkEquals && previous == value) {
            return
        }
        verify?.invoke(value)
        if (!this::profile.isInitialized) {
            val profile = profileManager.profiles[profileName] ?: return set(value)
            this.profile = profile
        }
        if (profile.initializing) {
            return set(value)
        }

        Log.log(LogMessageType.PROFILES, LogLevels.VERBOSE) { "Changed option $property in profile $profileName from ${get()} to $value" }
        if (!profile.reloading) {
            profileManager.profiles[profileName]?.saved = false
        }
        set(value)

        ProfilesChangeManager.onChange(profile, property.javaField ?: return, previous, value)
    }
}
