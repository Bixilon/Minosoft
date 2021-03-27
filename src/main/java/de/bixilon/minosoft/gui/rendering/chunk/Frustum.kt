package de.bixilon.minosoft.gui.rendering.chunk

import de.bixilon.minosoft.data.world.ChunkPosition
import de.bixilon.minosoft.gui.rendering.Camera
import de.bixilon.minosoft.gui.rendering.RenderConstants
import de.bixilon.minosoft.gui.rendering.util.VecUtil.rotate
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.func.cos
import glm_.func.rad
import glm_.func.sin
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
        val angle = (camera.fov - 90.0f).rad
        val sin = angle.sin
        val cos = angle.cos
        normals.add(camera.cameraFront.rotate(cameraRealUp, sin, cos).normalize())
        normals.add(camera.cameraFront.rotate(cameraRealUp, -sin, cos).normalize()) // negate angle -> negate sin
    }

    private fun calculateVerticalNormals() {
        val aspect = camera.renderWindow.screenDimensions.x / camera.renderWindow.screenDimensions.y // ToDo: x/y or y/x
        val angle = (camera.fov * aspect - 90.0f).rad
        val sin = angle.sin
        val cos = angle.cos
        normals.add(camera.cameraFront.rotate(camera.cameraRight, sin, cos).normalize())
        normals.add(camera.cameraFront.rotate(camera.cameraRight, -sin, cos).normalize()) // negate angle -> negate sin
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

            if (normal dot (min - camera.cameraPosition) < 0.0f) {
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
