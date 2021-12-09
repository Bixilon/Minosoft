package de.bixilon.minosoft.util.delegate

import de.bixilon.minosoft.util.KUtil.synchronizedMapOf
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.collections.SynchronizedMap
import de.bixilon.minosoft.util.delegate.delegate.Delegate
import de.bixilon.minosoft.util.delegate.delegate.entry.ListDelegate
import de.bixilon.minosoft.util.delegate.delegate.entry.MapDelegate
import de.bixilon.minosoft.util.delegate.delegate.entry.SetDelegate
import de.bixilon.minosoft.util.delegate.watcher.DelegateListener
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.MapChangeListener
import javafx.collections.SetChangeListener
import java.lang.ref.WeakReference
import kotlin.reflect.KClass
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0
import kotlin.reflect.KProperty1

object DelegateManager {
    // [field][<receiver>][reference][listener]
    private val listeners: SynchronizedMap<String, SynchronizedMap<WeakReference<Any>, MutableSet<Pair<WeakReference<Any>, DelegateListener<Any>>>>> = synchronizedMapOf()
    private val CLASS = Class.forName("kotlin.jvm.internal.CallableReference")
    private val RECEIVER_FIELD = CLASS.getDeclaredField("receiver")

    private val PROPERTY_1_CLASS = Class.forName("kotlin.reflect.jvm.internal.KPropertyImpl")
    private val CONTAINER_FIELD = PROPERTY_1_CLASS.getDeclaredField("container")

    private val KCLASS_CLASS = Class.forName("kotlin.reflect.jvm.internal.KClassImpl")
    private val JCLASS_FIELD = KCLASS_CLASS.getDeclaredField("jClass")

    init {
        RECEIVER_FIELD.isAccessible = true
        CONTAINER_FIELD.isAccessible = true
        JCLASS_FIELD.isAccessible = true
    }

    val KProperty<*>.receiver: Any
        get() = RECEIVER_FIELD.get(this)

    val KProperty<*>.container: KClass<*>
        get() = CONTAINER_FIELD.get(this).unsafeCast()

    val KClass<*>.jClass: Class<*>
        get() = JCLASS_FIELD.get(this).unsafeCast()

    val KProperty<*>.identifier: String
        get() = when (this) {
            is KProperty0<*> -> this.receiver::class.java.name + ":" + this.name
            is KProperty1<*, *> -> this.container.jClass.name + ":" + this.name
            else -> TODO("Can not identify $this")
        }

    fun <V> delegate(value: V, verify: ((V) -> Unit)? = null): Delegate<V> {
        return Delegate(value, verify)
    }

    fun <K, V> mapDelegate(default: MutableMap<K, V> = mutableMapOf(), verify: ((MapChangeListener.Change<out K, out V>) -> Unit)? = null): MapDelegate<K, V> {
        return MapDelegate(FXCollections.synchronizedObservableMap(FXCollections.observableMap(default)), verify)
    }

    fun <V> listDelegate(default: MutableList<V> = mutableListOf(), verify: ((ListChangeListener.Change<out V>) -> Unit)? = null): ListDelegate<V> {
        return ListDelegate(FXCollections.synchronizedObservableList(FXCollections.observableList(default)), verify = verify)
    }

    fun <V> setDelegate(default: MutableSet<V> = mutableSetOf(), verify: ((SetChangeListener.Change<out V>) -> Unit)? = null): SetDelegate<V> {
        return SetDelegate(FXCollections.synchronizedObservableSet(FXCollections.observableSet(default)), verify = verify)
    }

    fun onChange(thisRef: Any, field: String, previous: Any?, value: Any?) {
        val listeners = this.listeners[field] ?: return
        val toRemove: MutableSet<WeakReference<Any>> = mutableSetOf()
        for ((receiverReference, rest) in listeners) {
            val referenced = receiverReference.get()
            if (referenced == null) {
                toRemove += receiverReference
                continue
            }
            if (receiverReference.objectEquals(thisRef)) {
                val referenceToRemove: MutableSet<Pair<WeakReference<Any>, DelegateListener<Any>>> = mutableSetOf()
                for (pair in rest) {
                    val (referenceReference, listener) = pair
                    val reference = referenceReference.get()
                    if (reference == null) {
                        referenceToRemove += pair
                        continue
                    }
                    listener.invoke(previous, value)
                }
                rest -= referenceToRemove
            }
        }
        listeners -= toRemove
    }

    fun <T> register(reference: Any, listener: DelegateListener<T>) {
        val receiver = listener.property.receiver
        val receiverListeners = this.listeners.getOrPut(listener.field) { synchronizedMapOf() }
        var set: MutableSet<Pair<WeakReference<Any>, DelegateListener<Any>>>? = null
        for ((receiverReference, rest) in receiverListeners) {
            if (receiverReference.objectEquals(receiver)) {
                set = rest
                break
            }
        }
        if (set == null) {
            set = mutableSetOf()
            receiverListeners[WeakReference(receiver)] = set
        }
        set += Pair(WeakReference(reference), listener.unsafeCast())
    }

    fun WeakReference<*>.objectEquals(other: Any): Boolean {
        return this.get() == other
    }
}
