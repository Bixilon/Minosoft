/*
 * Minosoft
 * Copyright (C) 2020-2025 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.skeletal.instance

import de.bixilon.kmath.mat.mat4.f.MMat4f
import de.bixilon.kmath.mat.mat4.f.Mat4Operations
import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import java.nio.FloatBuffer

class TransformInstance(
    val id: Int,
    val pivot: Vec3f,
    val children: Map<String, TransformInstance>,
) {
    private val array = children.values.toTypedArray()
    val nPivot = -pivot
    val matrix = MMat4f()


    fun reset() {
        this.matrix.clearAssign()

        for (child in array) {
            child.reset()
        }
    }

    fun transform(parent: Mat4f) {
        Mat4Operations.times(parent, matrix.unsafe, matrix)

        for (child in array) {
            child.transform(this.matrix.unsafe)
        }
    }

    fun pack(buffer: FloatBuffer) {
        buffer.position(this.id * Mat4f.LENGTH)
        buffer.put(matrix._0.array, 0, Mat4f.LENGTH)

        for (child in this.array) {
            child.pack(buffer)
        }
    }

    operator fun get(name: String): TransformInstance? {
        return this.children[name]
    }
}
