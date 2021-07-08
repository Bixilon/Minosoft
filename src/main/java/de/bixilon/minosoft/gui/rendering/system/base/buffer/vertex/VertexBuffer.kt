package de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex

import kotlin.reflect.KClass

interface VertexBuffer {
    val vertices: Int
    val primitiveType: PrimitiveTypes
    val structure: KClass<*>

    fun draw()
}