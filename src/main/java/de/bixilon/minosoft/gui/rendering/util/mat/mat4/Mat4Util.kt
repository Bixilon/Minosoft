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

package de.bixilon.minosoft.gui.rendering.util.mat.mat4

import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.X
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.Y
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.Z

object Mat4Util {
    private val empty = Mat4()

    val Mat4.Companion.EMPTY_INSTANCE get() = empty

    fun Mat4.rotateDegreesAssign(rotation: Vec3): Mat4 {
        if (rotation.x != 0.0f) rotateAssign(rotation.x.rad, Vec3.X)
        if (rotation.y != 0.0f) rotateAssign(rotation.y.rad, Vec3.Y)
        if (rotation.z != 0.0f) rotateAssign(rotation.z.rad, Vec3.Z)
        return this
    }

    fun Mat4.rotateRadAssign(rotation: Vec3): Mat4 {
        if (rotation.x != 0.0f) rotateAssign(rotation.x, Vec3.X)
        if (rotation.y != 0.0f) rotateAssign(rotation.y, Vec3.Y)
        if (rotation.z != 0.0f) rotateAssign(rotation.z, Vec3.Z)
        return this
    }

    operator fun Mat4.times(vec3: Vec3): Vec3 {
        return this.times(vec3, vec3)
    }

    fun Mat4.times(vec3: Vec3, res: Vec3): Vec3 {
        val array = vec3.array
        res[0] = this[0, 0] * array[0] + this[1, 0] * array[1] + this[2, 0] * array[2] + this[3, 0]
        res[1] = this[0, 1] * array[0] + this[1, 1] * array[1] + this[2, 1] * array[2] + this[3, 1]
        res[2] = this[0, 2] * array[0] + this[1, 2] * array[1] + this[2, 2] * array[2] + this[3, 2]
        return res
    }

    fun Mat4.reset() {
        val array = this.array
        array[0] = 1.0f; array[1] = 0.0f;array[2] = 0.0f;array[3] = 0.0f
        array[4] = 0.0f; array[5] = 1.0f;array[6] = 0.0f;array[7] = 0.0f
        array[8] = 0.0f; array[9] = 0.0f;array[10] = 1.0f;array[11] = 0.0f
        array[12] = 0.0f; array[13] = 0.0f;array[14] = 0.0f;array[15] = 1.0f
    }
}
