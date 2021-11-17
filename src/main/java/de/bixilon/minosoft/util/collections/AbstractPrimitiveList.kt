package de.bixilon.minosoft.util.collections

abstract class AbstractPrimitiveList<T> : Clearable {
    var finished: Boolean = false
        protected set
    abstract val limit: Int
    abstract val size: Int
    abstract val isEmpty: Boolean

    protected abstract fun ensureSize(needed: Int)
    abstract fun add(value: T)

    abstract fun finish()

}
