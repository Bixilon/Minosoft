package de.bixilon.minosoft.gui.rendering.system.base.buffer.vertex

import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct

interface VertexBuffer {
    val vertices: Int
    val primitiveType: PrimitiveTypes
    val structure: MeshStruct

    fun draw()
}
