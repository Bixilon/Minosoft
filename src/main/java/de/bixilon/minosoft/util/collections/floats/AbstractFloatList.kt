package de.bixilon.minosoft.util.collections.floats

import de.bixilon.minosoft.util.collections.AbstractPrimitiveList

abstract class AbstractFloatList : AbstractPrimitiveList<Float>() {

    abstract fun addAll(floats: FloatArray)
    abstract fun addAll(floatList: AbstractFloatList)

    abstract fun toArray(): FloatArray
}
