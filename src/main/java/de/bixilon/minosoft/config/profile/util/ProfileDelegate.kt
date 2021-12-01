package de.bixilon.minosoft.config.profile.util

import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class ProfileDelegate<V>(
    private var value: V,
    private val checkEquals: Boolean,
    private val profileManager: ProfileManager<*>,
    private val profileName: String,
) : ReadWriteProperty<Any, V> {


    override fun getValue(thisRef: Any, property: KProperty<*>): V {
        return value
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: V) {
        if (checkEquals && this.value == value) {
            return
        }
        val profile = profileManager.profiles[profileName]
        if (profile == null || profile.initializing) {
            this.value = value
            return
        }

        Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Changed option $property in $thisRef in profile ${profileName::class.java} from ${this.value} to $value" }
        profileManager.profiles[profileName]?.saved = false

        // ToDo: Fire event
        this.value = value
    }
}
