package de.bixilon.minosoft.config.profile.change.listener

import de.bixilon.minosoft.config.profile.change.ProfilesChangeManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.util.KUtil.unsafeCast
import javafx.collections.MapChangeListener
import java.lang.reflect.Field
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

class MapProfileChangeListener<K, V>(
    override val property: KProperty<MutableMap<K, V>>,
    override val field: Field,
    override val profile: Profile?,
    private val callback: (MapChangeListener.Change<K, V>) -> Unit,
) : ProfileChangeListener<MutableMap<K, V>> {

    override fun invoke(previous: Any?, value: Any?) {
        callback(value.unsafeCast())
    }

    companion object {

        @JvmOverloads
        fun <K, V> KProperty<MutableMap<K, V>>.listenMap(reference: Any, profile: Profile? = null, callback: ((MapChangeListener.Change<K, V>) -> Unit)) {
            ProfilesChangeManager.register(reference, MapProfileChangeListener(this, javaField!!, profile, callback))
        }

        @JvmOverloads
        fun <K, V> KProperty<MutableMap<K, V>>.listenMapFX(reference: Any, profile: Profile? = null, callback: ((MapChangeListener.Change<K, V>) -> Unit)) {
            ProfilesChangeManager.register(reference, MapProfileChangeListener(this, javaField!!, profile) { JavaFXUtil.runLater { callback(it) } })
        }
    }
}
