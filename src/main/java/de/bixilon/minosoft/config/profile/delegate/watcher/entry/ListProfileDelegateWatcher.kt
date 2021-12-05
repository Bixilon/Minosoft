package de.bixilon.minosoft.config.profile.delegate.watcher.entry

import de.bixilon.minosoft.config.profile.delegate.ProfilesDelegateManager
import de.bixilon.minosoft.config.profile.delegate.watcher.ProfileDelegateWatcher
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.util.KUtil.unsafeCast
import javafx.collections.ListChangeListener
import java.lang.reflect.Field
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

class ListProfileDelegateWatcher<V>(
    override val property: KProperty<MutableList<V>>,
    override val field: Field,
    override val profile: Profile?,
    private val callback: (ListChangeListener.Change<V>) -> Unit,
) : ProfileDelegateWatcher<MutableList<V>> {

    override fun invoke(previous: Any?, value: Any?) {
        callback(value.unsafeCast())
    }

    companion object {

        @JvmOverloads
        fun <V> KProperty<MutableList<V>>.profileWatchList(reference: Any, profile: Profile? = null, callback: ((ListChangeListener.Change<V>) -> Unit)) {
            ProfilesDelegateManager.register(reference, ListProfileDelegateWatcher(this, javaField!!, profile, callback))
        }

        @JvmOverloads
        fun <V> KProperty<MutableList<V>>.profileWatchListFX(reference: Any, profile: Profile? = null, callback: ((ListChangeListener.Change<V>) -> Unit)) {
            ProfilesDelegateManager.register(reference, ListProfileDelegateWatcher(this, javaField!!, profile) { JavaFXUtil.runLater { callback(it) } })
        }
    }
}
