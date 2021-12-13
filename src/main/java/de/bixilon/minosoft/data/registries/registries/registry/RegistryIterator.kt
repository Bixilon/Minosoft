package de.bixilon.minosoft.data.registries.registries.registry

class RegistryIterator<T>(
    private var registry: AbstractRegistry<T>,
) : Iterator<T> {
    private var iterator = registry.noParentIterator()

    override fun hasNext(): Boolean {
        val hasNext = iterator.hasNext()
        if (hasNext) {
            return true
        }
        registry = registry.parent ?: return false
        iterator = registry.noParentIterator()
        return iterator.hasNext()
    }

    override fun next(): T {
        return iterator.next()
    }
}
