package de.bixilon.minosoft.config.profile.delegate.watcher.entry

import de.bixilon.minosoft.config.profile.delegate.ProfilesDelegateManager
import de.bixilon.minosoft.config.profile.delegate.watcher.ProfileDelegateWatcher
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.util.KUtil.unsafeCast
import javafx.collections.MapChangeListener
import java.lang.reflect.Field
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

class MapProfileDelegateWatcher<K, V>(
    override val property: KProperty<MutableMap<K, V>>,
    override val field: Field,
    override val profile: Profile?,
    private val callback: (MapChangeListener.Change<K, V>) -> Unit,
) : ProfileDelegateWatcher<MutableMap<K, V>> {

    override fun invoke(previous: Any?, value: Any?) {
        callback(value.unsafeCast())
    }

    companion object {

        @JvmOverloads
        fun <K, V> KProperty<MutableMap<K, V>>.profileWatchMap(reference: Any, profile: Profile? = null, callback: ((MapChangeListener.Change<K, V>) -> Unit)) {
            ProfilesDelegateManager.register(reference, MapProfileDelegateWatcher(this, javaField!!, profile, callback))
        }

        @JvmOverloads
        fun <K, V> KProperty<MutableMap<K, V>>.profileWatchMapFX(reference: Any, profile: Profile? = null, callback: ((MapChangeListener.Change<K, V>) -> Unit)) {
            ProfilesDelegateManager.register(reference, MapProfileDelegateWatcher(this, javaField!!, profile) { JavaFXUtil.runLater { callback(it) } })
        }
    }
}
