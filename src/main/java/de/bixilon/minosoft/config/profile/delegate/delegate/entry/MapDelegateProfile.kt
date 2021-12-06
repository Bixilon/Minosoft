package de.bixilon.minosoft.config.profile.delegate.delegate.entry

import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.delegate.ProfilesDelegateManager
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import javafx.collections.FXCollections
import javafx.collections.MapChangeListener
import javafx.collections.ObservableMap
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

open class MapDelegateProfile<K, V>(
    private var value: ObservableMap<K, V>,
    profileManager: ProfileManager<*>,
    profileName: String,
    private val verify: ((MapChangeListener.Change<out K, out V>) -> Unit)?,
) : ProfileEntryDelegate<MutableMap<K, V>>(profileManager, profileName) {

    init {
        initListener()
    }

    private fun initListener() {
        value.addListener(MapChangeListener {
            verify?.invoke(it)

            checkLateinitValues(null)

            if (!profileInitialized || profile.initializing) {
                return@MapChangeListener
            }


            if (StaticConfiguration.LOG_DELEGATE) {
                Log.log(LogMessageType.PROFILES, LogLevels.VERBOSE) { "Changed map entry $it in profile $profileName" }
            }
            if (!profile.reloading) {
                profileManager.profiles[profileName]?.saved = false
            }

            ProfilesDelegateManager.onChange(profile, property.javaField ?: return@MapChangeListener, null, it)
        })
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): MutableMap<K, V> {
        checkLateinitValues(property)
        return value
    }

    override fun get(): MutableMap<K, V> = value

    override fun set(value: MutableMap<K, V>) {
        if (!profileInitialized || profile.initializing || !profile.reloading) {
            this.value = FXCollections.synchronizedObservableMap(FXCollections.observableMap(value))
            initListener()
            profileManager.profiles[profileName]?.saved = false
            return
        }

        val checked: MutableSet<K> = mutableSetOf()
        for ((key, mapValue) in value) {
            checked += key
            val previous = this.value[key]
            val next = value[key]
            if (previous == next) {
                continue
            }
            this.value[key] = mapValue
        }
        val toRemove: MutableSet<K> = mutableSetOf()
        for (key in this.value.keys) {
            if (key in checked) {
                continue
            }
            toRemove += key
        }
        this.value -= toRemove
    }
}
