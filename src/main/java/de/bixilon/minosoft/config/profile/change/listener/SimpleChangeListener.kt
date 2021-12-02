package de.bixilon.minosoft.config.profile.change.listener

import de.bixilon.minosoft.config.profile.change.ProfilesChangeManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.util.KUtil.unsafeCast
import java.lang.reflect.Field
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.javaField

class SimpleChangeListener<T>(
    override val property: KProperty<T>,
    override val field: Field,
    override val profile: Profile?,
    instant: Boolean,
    private val callback: (T) -> Unit,
) : ProfileChangeListener<T> {

    init {
        if (instant) {
            when (property) {
                is KProperty0<*> -> invoke(property.get(), property.get())
                else -> TODO("Instant fire is not supported for ${property::class.java}")
            }
        }
    }

    override fun invoke(previous: Any?, value: Any?) {
        callback(value.unsafeCast())
    }

    companion object {

        @JvmOverloads
        fun <T> KProperty<T>.listen(reference: Any, instant: Boolean = false, profile: Profile? = null, callback: ((T) -> Unit)) {
            ProfilesChangeManager.register(reference, SimpleChangeListener(this, javaField!!, profile, instant, callback))
        }

        @JvmOverloads
        fun <T> KProperty<T>.listenFX(reference: Any, instant: Boolean = false, profile: Profile? = null, callback: ((T) -> Unit)) {
            ProfilesChangeManager.register(reference, SimpleChangeListener(this, javaField!!, profile, instant) { JavaFXUtil.runLater { callback(it) } })
        }
    }
}
