/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
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

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.EMPTY_INSTANCE
import java.nio.FloatBuffer

class TransformInstance(
    val id: Int,
    val pivot: Vec3,
    val children: Map<String, TransformInstance>,
) {
    val value = Mat4()

    fun pack(buffer: FloatBuffer) {
        pack(buffer, Mat4())
    }

    fun reset() {
        this.value(Mat4.EMPTY_INSTANCE)

        for ((name, child) in children) {
            child.reset()
        }
    }

    private fun pack(buffer: FloatBuffer, parent: Mat4) {
        val value = value * parent
        val offset = this.id * Mat4.length
        for (index in 0 until Mat4.length) {
            buffer.put(offset + index, value.array[index])
        }
        for ((name, child) in children) {
            child.pack(buffer, value)
        }
    }
}
