package de.bixilon.minosoft.util.delegate.watcher

import kotlin.reflect.KProperty

interface DelegateListener<T> {
    val property: KProperty<T>
    val field: String


    fun invoke(previous: Any?, value: Any?)
}
