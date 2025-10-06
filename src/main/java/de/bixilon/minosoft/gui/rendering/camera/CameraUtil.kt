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

package de.bixilon.minosoft.gui.rendering.camera

import de.bixilon.kmath.mat.mat4.f.MMat4f
import de.bixilon.kmath.mat.mat4.f.Mat4f
import de.bixilon.kmath.vec.vec3.f.Vec3f
import kotlin.math.abs
import kotlin.math.tan

object CameraUtil {

    fun perspective(fovY: Float, aspect: Float, near: Float, far: Float): Mat4f {
        assert(abs(aspect - Float.MIN_VALUE) > 0.0f)

        val tan = tan(fovY / 2.0f)

        val mat = MMat4f(0.0f)

        mat[0, 0] = 1.0f / (aspect * tan)
        mat[1, 1] = 1.0f / tan
        mat[2, 2] = -(far + near) / (far - near)
        mat[3, 2] = -1.0f
        mat[2, 3] = -(2.0f * far * near) / (far - near)

        return mat.unsafe
    }

    fun lookAt(eye: Vec3f, center: Vec3f, up: Vec3f): Mat4f {
        val f = (center - eye).normalize()
        val s = (f cross up).normalize()
        val u = s cross f

        val mat = MMat4f(1.0f)
        mat[0, 0] = s.x
        mat[0, 1] = s.y
        mat[0, 2] = s.z
        mat[1, 0] = u.x
        mat[1, 1] = u.y
        mat[1, 2] = u.z
        mat[2, 0] = -f.x
        mat[2, 1] = -f.y
        mat[2, 2] = -f.z
        mat[0, 3] = -(s dot eye)
        mat[1, 3] = -(u dot eye)
        mat[2, 3] = (f dot eye)

        return mat.unsafe
    }
}
