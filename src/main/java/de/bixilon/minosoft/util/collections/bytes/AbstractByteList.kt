package de.bixilon.minosoft.util.collections.bytes

import de.bixilon.minosoft.util.collections.AbstractPrimitiveList

abstract class AbstractByteList : AbstractPrimitiveList<Byte>() {

    abstract fun addAll(bytes: ByteArray)
    abstract fun addAll(byteList: AbstractByteList)

    abstract fun toArray(): ByteArray
}
