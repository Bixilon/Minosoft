package de.bixilon.minosoft.gui.rendering.framebuffer

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.util.mesh.Mesh
import de.bixilon.minosoft.gui.rendering.util.mesh.MeshStruct
import glm_.vec2.Vec2


class FramebufferMesh(renderWindow: RenderWindow) : Mesh(renderWindow, DefaultFramebufferMeshStruct) {

    init {
        val vertices = arrayOf(
            Vec2(-1.0f, -1.0f) to Vec2(0.0f, 1.0f),
            Vec2(-1.0f, +1.0f) to Vec2(0.0f, 0.0f),
            Vec2(+1.0f, +1.0f) to Vec2(1.0f, 0.0f),
            Vec2(+1.0f, -1.0f) to Vec2(1.0f, 1.0f),
        )
        for ((positionIndex, textureIndex) in this.order) {
            addVertex(vertices[positionIndex].first, vertices[textureIndex].second)
        }
    }

    private fun addVertex(position: Vec2, uv: Vec2) {
        data.add(position.x)
        data.add(position.y)
        data.add(uv.x)
        data.add(uv.y)
    }

    data class DefaultFramebufferMeshStruct(
        val position: Vec2,
        val uv: Vec2,
    ) {
        companion object : MeshStruct(DefaultFramebufferMeshStruct::class)
    }
}
