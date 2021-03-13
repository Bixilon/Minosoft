package de.bixilon.minosoft.gui.rendering.chunk

import de.bixilon.minosoft.data.world.ChunkPosition
import de.bixilon.minosoft.gui.rendering.Camera
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.util.VecUtil
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.glm
import glm_.vec3.Vec3

class Frustum(private val camera: Camera) {
    private val normals: MutableList<Vec3> = mutableListOf(
        camera.cameraFront.normalize(),
    )


    init {
        recalculate()
    }

    fun recalculate() {
        normals.clear()
        normals.add(camera.cameraFront.normalize())

        calculateSideNormals()
        calculateVerticalNormals()
    }

    private fun calculateSideNormals() {
        val cameraRealUp = (camera.cameraRight cross camera.cameraFront).normalize()
        val angle = glm.radians(camera.fov - 90f)
        val sin = glm.sin(angle)
        val cos = glm.cos(angle)
        normals.add(VecUtil.rotateVector(camera.cameraFront, cameraRealUp, sin, cos).normalize())
        normals.add(VecUtil.rotateVector(camera.cameraFront, cameraRealUp, -sin, cos).normalize()) // negate angle -> negate sin
    }

    private fun calculateVerticalNormals() {
        val aspect = camera.screenHeight.toFloat() / camera.screenWidth.toFloat()
        val angle = glm.radians(camera.fov * aspect - 90f)
        val sin = glm.sin(angle)
        val cos = glm.cos(angle)
        normals.add(VecUtil.rotateVector(camera.cameraFront, camera.cameraRight, sin, cos).normalize())
        normals.add(VecUtil.rotateVector(camera.cameraFront, camera.cameraRight, -sin, cos).normalize()) // negate angle -> negate sin
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

    fun containsChunk(chunkPosition: ChunkPosition, lowestBlockHeight: Int, highestBlockHeight: Int): Boolean {
        if (!RenderConstants.FRUSTUM_CULLING_ENABLED) {
            return true
        }
        val from = Vec3(chunkPosition.x * ProtocolDefinition.SECTION_WIDTH_X, lowestBlockHeight, chunkPosition.z * ProtocolDefinition.SECTION_WIDTH_Z)
        val to = from + Vec3(ProtocolDefinition.SECTION_WIDTH_X, highestBlockHeight, ProtocolDefinition.SECTION_WIDTH_Z)
        return containsRegion(from, to)
    }
}

private fun Vec3.dotProduct(vec3: Vec3): Float {
    return this.x * vec3.x + this.y * vec3.y + this.z * vec3.z
}
