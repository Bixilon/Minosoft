package de.bixilon.minosoft.config.profile.delegate.watcher

import de.bixilon.minosoft.config.profile.delegate.ProfilesDelegateManager
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.rendering.Rendering
import de.bixilon.minosoft.util.KUtil.unsafeCast
import java.lang.reflect.Field
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.jvm.javaField

class SimpleProfileDelegateLWatcher<T>(
    override val property: KProperty<T>,
    override val field: Field,
    override val profile: Profile?,
    instant: Boolean,
    private val callback: (T) -> Unit,
) : ProfileDelegateWatcher<T> {

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
        fun <T> KProperty<T>.profileWatch(reference: Any, instant: Boolean = false, profile: Profile? = null, callback: ((T) -> Unit)) {
            ProfilesDelegateManager.register(reference, SimpleProfileDelegateLWatcher(this, javaField!!, profile, instant, callback))
        }

        @JvmOverloads
        fun <T> KProperty<T>.profileWatchFX(reference: Any, instant: Boolean = false, profile: Profile? = null, callback: ((T) -> Unit)) {
            ProfilesDelegateManager.register(reference, SimpleProfileDelegateLWatcher(this, javaField!!, profile, instant) { JavaFXUtil.runLater { callback(it) } })
        }

        @JvmOverloads
        fun <T> KProperty<T>.profileWatchRendering(reference: Any, instant: Boolean = false, profile: Profile? = null, callback: ((T) -> Unit)) {
            val context = Rendering.currentContext ?: throw IllegalStateException("Can only be registered in a render context!")
            ProfilesDelegateManager.register(reference, SimpleProfileDelegateLWatcher(this, javaField!!, profile, instant) {
                val changeContext = Rendering.currentContext
                if (changeContext === context) {
                    callback(it)
                } else {
                    context.queue += { callback(it) }
                }
            })
        }
    }
}
