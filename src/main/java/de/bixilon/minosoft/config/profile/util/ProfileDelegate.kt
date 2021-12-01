package de.bixilon.minosoft.config.profile.util

import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.change.ProfilesChangeManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

open class ProfileDelegate<V>(
    private var value: V,
    private val checkEquals: Boolean,
    private val profileManager: ProfileManager<*>,
    private val profileName: String,
) : ReadWriteProperty<Any, V> {
    private lateinit var profile: Profile

    override fun getValue(thisRef: Any, property: KProperty<*>): V {
        return value
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: V) {
        if (checkEquals && this.value == value) {
            return
        }
        if (!this::profile.isInitialized) {
            val profile = profileManager.profiles[profileName]
            if (profile == null) {
                this.value = value
                return
            }
            this.profile = profile
        }
        if (profile.initializing) {
            this.value = value
            return
        }

        Log.log(LogMessageType.PROFILES, LogLevels.VERBOSE) { "Changed option $property in profile $profileName from ${this.value} to $value" }
        profileManager.profiles[profileName]?.saved = false
        val previous = this.value
        this.value = value

        ProfilesChangeManager.onChange(profile, property.javaField ?: return, previous, value)
    }
}
