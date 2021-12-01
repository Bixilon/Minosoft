package de.bixilon.minosoft.config.config2.util

import de.bixilon.minosoft.config.config2.ProfileManager
import de.bixilon.minosoft.util.KUtil.realName
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

open class ConfigDelegate<V>(
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
        Log.log(LogMessageType.OTHER, LogLevels.VERBOSE) { "Changed option $property in ${thisRef::class.java.realName} in profile $profileName from ${this.value} to $value" }

        // ToDo: Fire event, save config
        this.value = value
    }
}
