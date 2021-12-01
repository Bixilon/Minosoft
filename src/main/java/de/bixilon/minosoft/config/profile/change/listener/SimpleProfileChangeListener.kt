package de.bixilon.minosoft.config.profile.change.listener

import de.bixilon.minosoft.config.profile.change.ProfilesChangeManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import java.lang.reflect.Field
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.javaField

class SimpleProfileChangeListener<T>(
    override val property: KProperty<T>,
    override val field: Field,
    override val profile: Profile?,
    private val callback: (T) -> Unit,
) : ProfileChangeListener<T> {

    override fun invoke(previous: T, value: T) {
        callback(value)
    }

    companion object {

        fun <T> KProperty<T>.listen(reference: Any, profile: Profile? = null, callback: ((T) -> Unit)) {
            ProfilesChangeManager.register(reference, SimpleProfileChangeListener(this, javaField!!, profile, callback))
        }

        fun <T> KProperty<T>.listenFX(reference: Any, profile: Profile? = null, callback: ((T) -> Unit)) {
            ProfilesChangeManager.register(reference, SimpleProfileChangeListener(this, javaField!!, profile) { JavaFXUtil.runLater { callback(it) } })
        }
    }
}
