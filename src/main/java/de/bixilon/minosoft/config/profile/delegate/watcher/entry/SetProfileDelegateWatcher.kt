package de.bixilon.minosoft.config.profile.delegate.watcher.entry

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.profile.delegate.ProfilesDelegateManager
import de.bixilon.minosoft.config.profile.delegate.watcher.ProfileDelegateWatcher
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.util.delegate.DelegateManager.identifier
import javafx.collections.SetChangeListener
import kotlin.reflect.KProperty

class SetProfileDelegateWatcher<V>(
    override val property: KProperty<MutableSet<V>>,
    override val profile: Profile?,
    private val callback: (SetChangeListener.Change<V>) -> Unit,
) : ProfileDelegateWatcher<MutableSet<V>> {
    override val fieldIdentifier = property.identifier

    override fun invoke(previous: Any?, value: Any?) {
        callback(value.unsafeCast())
    }

    companion object {

        @JvmOverloads
        fun <V> KProperty<MutableSet<V>>.profileWatchSet(reference: Any, profile: Profile? = null, callback: ((SetChangeListener.Change<V>) -> Unit)) {
            ProfilesDelegateManager.register(reference, SetProfileDelegateWatcher(this, profile, callback))
        }

        @JvmOverloads
        fun <V> KProperty<MutableSet<V>>.profileWatchSetFX(reference: Any, profile: Profile? = null, callback: ((SetChangeListener.Change<V>) -> Unit)) {
            ProfilesDelegateManager.register(reference, SetProfileDelegateWatcher(this, profile) { JavaFXUtil.runLater { callback(it) } })
        }
    }
}
