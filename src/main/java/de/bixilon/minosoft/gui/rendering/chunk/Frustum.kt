package de.bixilon.minosoft.gui.rendering.chunk

import de.bixilon.minosoft.data.world.ChunkPosition
import de.bixilon.minosoft.gui.rendering.Camera
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec3.Vec3

class Frustum(private val camera: Camera) {
    private val normals: Array<Vec3>

    init {
        val realFront = Vec3(camera.cameraFront)
        // realFront.y = 0f
        realFront.normalize()
        // val left = BlockModelElement.rotateVector(realFront, -glm.radians(camera.fov.toDouble() - 90), Axes.Y).normalize()
        // val right = BlockModelElement.rotateVector(realFront, glm.radians(camera.fov.toDouble() - 90), Axes.Y).normalize()
        // TODO: up, down, left, right, not working correctly
        normals = arrayOf(
            camera.cameraFront.normalize(),
            // left.normalize(),
            // right.normalize(),
        )
    }

    private fun containsRegion(from: Vec3, to: Vec3): Boolean {
        val min = Vec3()
        for (normal in normals) {
            // get the point most likely to be in the frustum
            min.x = if (normal.x < 0) {
                from.x
            } else {
                to.x
            }
            min.y = if (normal.y < 0) {
                from.y
            } else {
                to.y
            }
            min.z = if (normal.z < 0) {
                from.z
            } else {
                to.z
            }

            if (normal.dotProduct(min - camera.cameraPosition) < 0f) {
                return false // region is outside of frustum
            }
        }
        return true
    }

    fun containsChunk(chunkPosition: ChunkPosition, connection: Connection): Boolean {
        val dimension = connection.player.world.dimension!!
        val from = Vec3(chunkPosition.x * ProtocolDefinition.SECTION_WIDTH_X, dimension.minY, chunkPosition.z * ProtocolDefinition.SECTION_WIDTH_Z)
        val to = from + Vec3(ProtocolDefinition.SECTION_WIDTH_X, dimension.logicalHeight, ProtocolDefinition.SECTION_WIDTH_Z)
        val frustum = Frustum(connection.renderer.renderWindow.camera)
        return frustum.containsRegion(from, to)
    }
}

private fun Vec3.dotProduct(vec3: Vec3): Float {
    return this.x * vec3.x + this.y * vec3.y + this.z * vec3.z
}
