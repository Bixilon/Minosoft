package de.bixilon.minosoft.gui.rendering.chunk

import glm_.mat4x4.Mat4
import glm_.vec3.Vec3

class Frustum(matrix: Mat4) {
    val normals =
        arrayOf(
            Vec3(
                matrix.a3 + matrix.a0,
                matrix.b3 + matrix.b0,
                matrix.c3 + matrix.c0).normalize(),
            Vec3()
               )
}
