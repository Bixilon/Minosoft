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

import de.bixilon.minosoft.data.world.vec.mat4.f.Mat4f
import de.bixilon.minosoft.data.world.vec.vec3.f.Vec3f
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.math.tan

object CameraUtil {

    fun perspective(fovY: Float, aspect: Float, near: Float, far: Float): Mat4f {
        assert(abs(aspect - Float.MIN_VALUE) > 0f)

        val tanHalfFovy = tan(fovY / 2f)

        res put 0f
        res[0, 0] = 1f / (aspect * tanHalfFovy)
        res[1, 1] = 1f / (tanHalfFovy)
        res[2, 2] = -(far + near) / (far - near)
        res[2, 3] = -1f
        res[3, 2] = -(2f * far * near) / (far - near)
        return res
    }

    fun lookAt(eye: Vec3f, center: Vec3f, up: Vec3f): Mat4f {
        // f = normalize(center - eye)
        var fX = center.x - eye.x
        var fY = center.y - eye.y
        var fZ = center.z - eye.z
        var inv = 1f / sqrt(fX * fX + fY * fY + fZ * fZ)
        fX *= inv
        fY *= inv
        fZ *= inv
        // s = normalize(cross(f, up))
        var sX = fY * up.z - up.y * fZ
        var sY = fZ * up.x - up.z * fX
        var sZ = fX * up.y - up.x * fY
        inv = 1f / sqrt(sX * sX + sY * sY + sZ * sZ)
        sX *= inv
        sY *= inv
        sZ *= inv
        // u = cross(s, f)
        val uX = sY * fZ - fY * sZ
        val uY = sZ * fX - fZ * sX
        val uZ = sX * fY - fX * sY

        res put 1f
        res[0, 0] = sX
        res[1, 0] = sY
        res[2, 0] = sZ
        res[0, 1] = uX
        res[1, 1] = uY
        res[2, 1] = uZ
        res[0, 2] = -fX
        res[1, 2] = -fY
        res[2, 2] = -fZ
//        res[3,0] =-dot(s, eye)
        res[3, 0] = -(sX * eye.x + sY * eye.y + sZ * eye.z)
//        res[3,1] =-dot(u, eye)
        res[3, 1] = -(uX * eye.x + uY * eye.y + uZ * eye.z)
//        res[3,2] = dot(f, eye)
        res[3, 2] = fX * eye.x + fY * eye.y + fZ * eye.z
        return res
    }
}
