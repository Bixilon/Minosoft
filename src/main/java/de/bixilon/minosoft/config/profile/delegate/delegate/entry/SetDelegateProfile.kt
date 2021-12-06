package de.bixilon.minosoft.config.profile.delegate.delegate.entry

import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.delegate.ProfilesDelegateManager
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import javafx.collections.FXCollections
import javafx.collections.ObservableSet
import javafx.collections.SetChangeListener
import kotlin.reflect.jvm.javaField

open class SetDelegateProfile<V>(
    private var value: ObservableSet<V>,
    profileManager: ProfileManager<*>,
    profileName: String,
    private val verify: ((SetChangeListener.Change<out V>) -> Unit)?,
) : ProfileEntryDelegate<MutableSet<V>>(profileManager, profileName) {

    init {
        initListener()
    }

    private fun initListener() {
        value.addListener(SetChangeListener {
            verify?.invoke(it)
            checkLateinitValues(null)

            if (profile.initializing) {
                return@SetChangeListener
            }

            if (StaticConfiguration.LOG_DELEGATE) {
                Log.log(LogMessageType.PROFILES, LogLevels.VERBOSE) { "Changed set entry $it in profile $profileName" }
            }
            if (!profile.reloading) {
                profileManager.profiles[profileName]?.saved = false
            }

            ProfilesDelegateManager.onChange(profile, property.javaField ?: return@SetChangeListener, null, it)
        })
    }

    override fun get(): MutableSet<V> = value

    override fun set(value: MutableSet<V>) {
        this.value = FXCollections.synchronizedObservableSet(FXCollections.observableSet(value))
        initListener()
        if (!profile.reloading) {
            profileManager.profiles[profileName]?.saved = false
        }
    }
}
