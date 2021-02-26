package de.bixilon.minosoft.gui.rendering.chunk

import de.bixilon.minosoft.data.world.ChunkLocation
import de.bixilon.minosoft.gui.rendering.Camera
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec3.Vec3

class Frustum(val camera: Camera) {
    val normals: Array<Vec3>

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
            min.x = if (normal.x < 0) from.x else to.x
            min.y = if (normal.y < 0) from.y else to.y
            min.z = if (normal.z < 0) from.z else to.z

            if (dotProduct(normal, min - camera.cameraPosition) < 0f) {
                return false // region lies outside of frustum
            }
        }
        return true
    }

    private fun dotProduct(v1: Vec3, v2: Vec3): Float {
        return v1.x * v2.x + v1.y * v2.y + v1.z * v2.z
    }

    fun containsChunk(chunkLocation: ChunkLocation, connection: Connection): Boolean {
        val from = Vec3(chunkLocation.x * ProtocolDefinition.SECTION_WIDTH_X, connection.player.world.dimension!!.minY, chunkLocation.z * ProtocolDefinition.SECTION_WIDTH_Z)
        val to = from + Vec3(ProtocolDefinition.SECTION_WIDTH_X, connection.player.world.dimension!!.logicalHeight, ProtocolDefinition.SECTION_WIDTH_Z)
        val frustum = Frustum(connection.renderer.renderWindow.camera)
        return frustum.containsRegion(from, to)
    }
}
