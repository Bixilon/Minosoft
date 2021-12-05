package de.bixilon.minosoft.config.profile.delegate.delegate.entry

import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.delegate.delegate.DelegateSetter
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class ProfileEntryDelegate<V>(
    protected val profileManager: ProfileManager<*>,
    protected val profileName: String,
) : ReadWriteProperty<Any, V>, DelegateSetter<V> {
    protected lateinit var profile: Profile
    protected lateinit var property: KProperty<V>

    protected val profileInitialized: Boolean
        get() = this::profile.isInitialized


    protected fun checkLateinitValues(property: KProperty<*>?) {
        if (!this::profile.isInitialized) {
            profileManager.profiles[profileName]?.let { this.profile = it }
        }
        if (property != null && !this::property.isInitialized) {
            this.property = property.unsafeCast()
        }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): V {
        checkLateinitValues(property)
        return get()
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: V) {
        checkLateinitValues(property)
        set(value)
    }
}
