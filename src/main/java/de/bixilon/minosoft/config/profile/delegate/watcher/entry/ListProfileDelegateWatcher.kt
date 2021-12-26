package de.bixilon.minosoft.config.profile.delegate.watcher.entry

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.profile.delegate.ProfilesDelegateManager
import de.bixilon.minosoft.config.profile.delegate.watcher.ProfileDelegateWatcher
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.util.delegate.DelegateManager.identifier
import javafx.collections.ListChangeListener
import kotlin.reflect.KProperty

class ListProfileDelegateWatcher<V>(
    override val property: KProperty<MutableList<V>>,
    override val profile: Profile?,
    private val callback: (ListChangeListener.Change<V>) -> Unit,
) : ProfileDelegateWatcher<MutableList<V>> {
    override val fieldIdentifier = property.identifier

    override fun invoke(previous: Any?, value: Any?) {
        callback(value.unsafeCast())
    }

    companion object {

        @JvmOverloads
        fun <V> KProperty<MutableList<V>>.profileWatchList(reference: Any, profile: Profile? = null, callback: ((ListChangeListener.Change<V>) -> Unit)) {
            ProfilesDelegateManager.register(reference, ListProfileDelegateWatcher(this, profile, callback))
        }

        @JvmOverloads
        fun <V> KProperty<MutableList<V>>.profileWatchListFX(reference: Any, profile: Profile? = null, callback: ((ListChangeListener.Change<V>) -> Unit)) {
            ProfilesDelegateManager.register(reference, ListProfileDelegateWatcher(this, profile) { JavaFXUtil.runLater { callback(it) } })
        }
    }
}
