package de.bixilon.minosoft.gui.rendering.world.mesh

import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.util.KUtil.format
import glm_.vec3.Vec3

class VisibleMeshes(val cameraPosition: Vec3 = Vec3.EMPTY) {
    val opaque: MutableList<SingleWorldMesh> = mutableListOf()
    val translucent: MutableList<SingleWorldMesh> = mutableListOf()
    val transparent: MutableList<SingleWorldMesh> = mutableListOf()

    val sizeString: String
        get() = "${opaque.size.format()}|${translucent.size.format()}|${transparent.size.format()}"


    fun addMesh(mesh: WorldMesh) {
        val distance = (cameraPosition - mesh.center).length2()
        mesh.opaqueMesh?.let {
            it.distance = distance
            opaque += it
        }
        mesh.translucentMesh?.let {
            it.distance = distance
            translucent += it
        }
        mesh.transparentMesh?.let {
            it.distance = distance
            transparent += it
        }
    }


    fun sort() {
        opaque.sortBy { it.distance }
        translucent.sortBy { -it.distance }
        transparent.sortBy { it.distance }
    }


    fun removeMesh(mesh: WorldMesh) {
        mesh.opaqueMesh?.let { opaque -= it }
        mesh.translucentMesh?.let { translucent -= it }
        mesh.transparentMesh?.let { transparent -= it }
    }

    fun clear() {
        opaque.clear()
        translucent.clear()
        transparent.clear()
    }
}
