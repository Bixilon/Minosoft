package de.bixilon.minosoft.config.profile.delegate.watcher.entry

import de.bixilon.minosoft.config.profile.delegate.ProfilesDelegateManager
import de.bixilon.minosoft.config.profile.delegate.watcher.ProfileDelegateWatcher
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.util.KUtil.unsafeCast
import javafx.collections.SetChangeListener
import java.lang.reflect.Field
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

class SetProfileDelegateWatcher<V>(
    override val property: KProperty<MutableSet<V>>,
    override val field: Field,
    override val profile: Profile?,
    private val callback: (SetChangeListener.Change<V>) -> Unit,
) : ProfileDelegateWatcher<MutableSet<V>> {

    override fun invoke(previous: Any?, value: Any?) {
        callback(value.unsafeCast())
    }

    companion object {

        @JvmOverloads
        fun <V> KProperty<MutableSet<V>>.profileWatchSet(reference: Any, profile: Profile? = null, callback: ((SetChangeListener.Change<V>) -> Unit)) {
            ProfilesDelegateManager.register(reference, SetProfileDelegateWatcher(this, javaField!!, profile, callback))
        }

        @JvmOverloads
        fun <V> KProperty<MutableSet<V>>.profileWatchSetFX(reference: Any, profile: Profile? = null, callback: ((SetChangeListener.Change<V>) -> Unit)) {
            ProfilesDelegateManager.register(reference, SetProfileDelegateWatcher(this, javaField!!, profile) { JavaFXUtil.runLater { callback(it) } })
        }
    }
}
