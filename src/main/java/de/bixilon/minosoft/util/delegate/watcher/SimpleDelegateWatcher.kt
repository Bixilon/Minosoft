package de.bixilon.minosoft.util.delegate.watcher

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.rendering.Rendering
import de.bixilon.minosoft.util.delegate.DelegateManager
import de.bixilon.minosoft.util.delegate.DelegateManager.identifier
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

class SimpleDelegateWatcher<T>(
    override val property: KProperty<T>,
    instant: Boolean,
    private val callback: (T) -> Unit,
) : DelegateListener<T> {
    override val field: String = property.identifier

    init {
        check(property !is KProperty1<*, *>) { "Can only listen on instanced, not classes!" }
        check(property is KProperty0<*>) { "Can only listen on delegates!" }
        if (instant) {
            invoke(property.get(), property.get())
        }
    }

    override fun invoke(previous: Any?, value: Any?) {
        callback(value.unsafeCast())
    }

    companion object {

        @JvmOverloads
        fun <T> KProperty<T>.watch(reference: Any, instant: Boolean = false, callback: ((T) -> Unit)) {
            DelegateManager.register(reference, SimpleDelegateWatcher(this, instant, callback))
        }

        @JvmOverloads
        fun <T> KProperty<T>.watchFX(reference: Any, instant: Boolean = false, callback: ((T) -> Unit)) {
            DelegateManager.register(reference, SimpleDelegateWatcher(this, instant) { JavaFXUtil.runLater { callback(it) } })
        }

        @JvmOverloads
        fun <T> KProperty<T>.watchRendering(reference: Any, instant: Boolean = false, callback: ((T) -> Unit)) {
            val context = Rendering.currentContext ?: throw IllegalStateException("Can only be registered in a render context!")
            DelegateManager.register(reference, SimpleDelegateWatcher(this, instant) {
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
