package de.bixilon.minosoft.config.profile.profiles.eros

import com.google.common.collect.HashBiMap
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.profile.GlobalProfileManager
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.delegate.delegate.BackingDelegate
import de.bixilon.minosoft.config.profile.delegate.delegate.ProfileDelegate
import de.bixilon.minosoft.config.profile.delegate.delegate.entry.ListDelegateProfile
import de.bixilon.minosoft.config.profile.delegate.delegate.entry.MapDelegateProfile
import de.bixilon.minosoft.config.profile.delegate.delegate.entry.SetDelegateProfile
import de.bixilon.minosoft.modding.event.master.GlobalEventMaster
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.MapChangeListener
import javafx.collections.SetChangeListener
import org.kordamp.ikonli.fontawesome5.FontAwesomeSolid
import java.util.concurrent.locks.ReentrantLock

object ErosProfileManager : ProfileManager<ErosProfile> {
    override val namespace = "minosoft:eros".toResourceLocation()
    override val latestVersion = 1
    override val saveLock = ReentrantLock()
    override val profileClass = ErosProfile::class.java
    override val profileSelectable: Boolean
        get() = false
    override val icon = FontAwesomeSolid.WINDOW_RESTORE


    override var currentLoadingPath: String? = null
    override val profiles: HashBiMap<String, ErosProfile> = HashBiMap.create()

    override var selected: ErosProfile = null.unsafeCast()
        set(value) {
            field = value
            GlobalProfileManager.selectProfile(this, value)
            GlobalEventMaster.fireEvent(ErosProfileSelectEvent(value))
        }

    override fun createProfile(name: String, description: String?): ErosProfile {
        currentLoadingPath = name
        val profile = ErosProfile(description ?: "Default eros profile")
        currentLoadingPath = null
        profiles[name] = profile

        return profile
    }

    override fun <V> delegate(value: V, verify: ((V) -> Unit)?): ProfileDelegate<V> {
        return ProfileDelegate(value, this, currentLoadingPath ?: getName(selected), verify)
    }

    override fun <V> backingDelegate(verify: ((V) -> Unit)?, getter: () -> V, setter: (V) -> Unit): BackingDelegate<V> {
        return object : BackingDelegate<V>(this, currentLoadingPath ?: getName(selected), verify) {
            override fun get(): V = getter()

            override fun set(value: V) = setter(value)
        }
    }

    override fun <K, V> mapDelegate(default: MutableMap<K, V>, verify: ((MapChangeListener.Change<out K, out V>) -> Unit)?): MapDelegateProfile<K, V> {
        return MapDelegateProfile(FXCollections.synchronizedObservableMap(FXCollections.observableMap(default)), profileManager = this, profileName = currentLoadingPath ?: getName(selected), verify = verify)
    }

    override fun <V> listDelegate(default: MutableList<V>, verify: ((ListChangeListener.Change<out V>) -> Unit)?): ListDelegateProfile<V> {
        return ListDelegateProfile(FXCollections.synchronizedObservableList(FXCollections.observableList(default)), profileManager = this, profileName = currentLoadingPath ?: getName(selected), verify = verify)
    }

    override fun <V> setDelegate(default: MutableSet<V>, verify: ((SetChangeListener.Change<out V>) -> Unit)?): SetDelegateProfile<V> {
        return SetDelegateProfile(FXCollections.synchronizedObservableSet(FXCollections.observableSet(default)), profileManager = this, profileName = currentLoadingPath ?: getName(selected), verify = verify)
    }
}
