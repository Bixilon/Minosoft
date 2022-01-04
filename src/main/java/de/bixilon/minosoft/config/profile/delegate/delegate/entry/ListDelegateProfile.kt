package de.bixilon.minosoft.config.profile.delegate.delegate.entry

import de.bixilon.kutil.watcher.WatchUtil.identifier
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.delegate.ProfilesDelegateManager
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList

open class ListDelegateProfile<V>(
    private var value: ObservableList<V>,
    profileManager: ProfileManager<*>,
    profileName: String,
    private val verify: ((ListChangeListener.Change<out V>) -> Unit)?,
) : ProfileEntryDelegate<MutableList<V>>(profileManager, profileName) {

    init {
        initListener()
    }

    private fun initListener() {
        value.addListener(ListChangeListener {
            verify?.invoke(it)
            checkLateinitValues(null)

            if (!this.profileInitialized || profile.initializing) {
                return@ListChangeListener
            }
            if (StaticConfiguration.LOG_DELEGATE) {
                Log.log(LogMessageType.PROFILES, LogLevels.VERBOSE) { "Changed list entry $it in profile $profileName" }
            }
            if (!profile.reloading) {
                profileManager.profiles[profileName]?.saved = false
            }

            ProfilesDelegateManager.onChange(profile, property.identifier, null, it)
        })
    }

    override fun get(): MutableList<V> = value

    override fun set(value: MutableList<V>) {
        this.value = FXCollections.synchronizedObservableList(FXCollections.observableList(value))
        initListener()
        if (!profile.reloading) {
            profileManager.profiles[profileName]?.saved = false
        }
    }
}
