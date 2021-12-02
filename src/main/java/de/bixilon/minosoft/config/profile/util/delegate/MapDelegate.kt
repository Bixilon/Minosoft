package de.bixilon.minosoft.config.profile.util.delegate

import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.change.ProfilesChangeManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import javafx.collections.ObservableMap
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

open class MapDelegate<K, V>(
    private var value: ObservableMap<K, V> = FXCollections.synchronizedObservableMap(FXCollections.observableHashMap()),
    private val profileManager: ProfileManager<*>,
    private val profileName: String,
    private val verify: ((MapChangeListener.Change<out K, out V>) -> Unit)?,
) : ReadWriteProperty<Any, MutableMap<K, V>> {
    private lateinit var profile: Profile
    private lateinit var property: KProperty<MutableMap<K, V>>

    init {
        initListener()
    }

    private fun initListener() {
        value.addListener(MapChangeListener {
            verify?.invoke(it)

            if (!this::profile.isInitialized) {
                val profile = profileManager.profiles[profileName] ?: return@MapChangeListener
                this.profile = profile
            }
            if (profile.initializing) {
                return@MapChangeListener
            }


            Log.log(LogMessageType.PROFILES, LogLevels.VERBOSE) { "Changed map entry $it in profile $profileName" }
            profileManager.profiles[profileName]?.saved = false

            ProfilesChangeManager.onChange(profile, property.javaField ?: return@MapChangeListener, null, it)
        })
    }

    private fun checkLateinitValues(property: KProperty<*>) {
        if (!this::profile.isInitialized) {
            profileManager.profiles[profileName]?.let { this.profile = it }
        }
        if (!this::property.isInitialized) {
            this.property = property.unsafeCast()
        }
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): MutableMap<K, V> {
        checkLateinitValues(property)
        return value
    }

    override fun setValue(thisRef: Any, property: KProperty<*>, value: MutableMap<K, V>) {
        checkLateinitValues(property)
        this.value = FXCollections.synchronizedObservableMap(FXCollections.observableMap(value))
        initListener()
    }
}
