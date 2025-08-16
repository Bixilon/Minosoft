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

package de.bixilon.minosoft.gui.rendering.util.mat.mat4

import de.bixilon.minosoft.data.world.vec.mat4.f.Mat4f
import de.bixilon.minosoft.data.world.vec.vec3.f.Vec3f
import de.bixilon.minosoft.util.KUtil.rad

object Mat4Util {

    fun Mat4f.rotateDegreesAssign(rotation: Vec3f): Mat4f {
        if (rotation.x != 0.0f) rotateX(this, rotation.x.rad)
        if (rotation.y != 0.0f) rotateY(this, rotation.y.rad)
        if (rotation.z != 0.0f) rotateZ(this, rotation.z.rad)
        return this
    }

    fun Mat4f.rotateRadAssign(rotation: Vec3f): Mat4f {
        if (rotation.x != 0.0f) rotateX(this, rotation.x)
        if (rotation.y != 0.0f) rotateY(this, rotation.y)
        if (rotation.z != 0.0f) rotateZ(this, rotation.z)
        return this
    }

    fun Mat4f.times(vec3: Vec3f, res: Vec3f): Vec3f {
        val array = vec3.array
        res[0] = this[0, 0] * array[0] + this[1, 0] * array[1] + this[2, 0] * array[2] + this[3, 0]
        res[1] = this[0, 1] * array[0] + this[1, 1] * array[1] + this[2, 1] * array[2] + this[3, 1]
        res[2] = this[0, 2] * array[0] + this[1, 2] * array[1] + this[2, 2] * array[2] + this[3, 2]
        return res
    }

    fun Mat4f.rotateXAssign(rad: Float): Mat4f {
        rotateX(this, rad)

        return this
    }

    fun rotateX(m: Mat4f, angle: Float) {
        val c = glm.cos(angle)
        val s = glm.sin(angle)


        val tempX = (1f - c)
        val rotate00 = c + tempX
        val rotate21 = -s

        m[0, 0] = m[0, 0] * rotate00
        m[0, 1] = m[0, 1] * rotate00
        m[0, 2] = m[0, 2] * rotate00
        m[0, 3] = m[0, 3] * rotate00

        val res1x = m[1, 0] * c + m[2, 0] * s
        val res1y = m[1, 1] * c + m[2, 1] * s
        val res1z = m[1, 2] * c + m[2, 2] * s
        val res1w = m[1, 3] * c + m[2, 3] * s

        m[2, 0] = m[1, 0] * rotate21 + m[2, 0] * c
        m[2, 1] = m[1, 1] * rotate21 + m[2, 1] * c
        m[2, 2] = m[1, 2] * rotate21 + m[2, 2] * c
        m[2, 3] = m[1, 3] * rotate21 + m[2, 3] * c

        m[1, 0] = res1x
        m[1, 1] = res1y
        m[1, 2] = res1z
        m[1, 3] = res1w
    }

    fun rotateY(m: Mat4f, angle: Float) {
        val c = glm.cos(angle)
        val s = glm.sin(angle)


        val tempY = (1f - c)
        val rotate02 = -s
        val rotate11 = c + tempY


        val res0x = m[0, 0] * c + m[2, 0] * rotate02
        val res0y = m[0, 1] * c + m[2, 1] * rotate02
        val res0z = m[0, 2] * c + m[2, 2] * rotate02
        val res0w = m[0, 3] * c + m[2, 3] * rotate02

        m[1, 0] = m[1, 0] * rotate11
        m[1, 1] = m[1, 1] * rotate11
        m[1, 2] = m[1, 2] * rotate11
        m[1, 3] = m[1, 3] * rotate11

        m[2, 0] = m[0, 0] * s + m[2, 0] * c
        m[2, 1] = m[0, 1] * s + m[2, 1] * c
        m[2, 2] = m[0, 2] * s + m[2, 2] * c
        m[2, 3] = m[0, 3] * s + m[2, 3] * c

        m[0, 0] = res0x
        m[0, 1] = res0y
        m[0, 2] = res0z
        m[0, 3] = res0w
    }

    fun rotateZ(m: Mat4f, angle: Float) {
        val c = glm.cos(angle)
        val s = glm.sin(angle)

        val tempZ = (1f - c)
        val rotate10 = -s
        val rotate22 = c + tempZ


        val res0x = m[0, 0] * c + m[1, 0] * s
        val res0y = m[0, 1] * c + m[1, 1] * s
        val res0z = m[0, 2] * c + m[1, 2] * s
        val res0w = m[0, 3] * c + m[1, 3] * s

        m[1, 0] = m[0, 0] * rotate10 + m[1, 0] * c
        m[1, 1] = m[0, 1] * rotate10 + m[1, 1] * c
        m[1, 2] = m[0, 2] * rotate10 + m[1, 2] * c
        m[1, 3] = m[0, 3] * rotate10 + m[1, 3] * c

        m[2, 0] = m[2, 0] * rotate22
        m[2, 1] = m[2, 1] * rotate22
        m[2, 2] = m[2, 2] * rotate22
        m[2, 3] = m[2, 3] * rotate22

        m[0, 0] = res0x
        m[0, 1] = res0y
        m[0, 2] = res0z
        m[0, 3] = res0w
    }

    fun Mat4f.translateXAssign(vX: Float): Mat4f {
        this[3, 0] += this[0, 0] * vX
        this[3, 1] += this[0, 1] * vX
        this[3, 2] += this[0, 2] * vX
        this[3, 3] += this[0, 3] * vX
        return this
    }

    fun Mat4f.translateYAssign(vY: Float): Mat4f {
        this[3, 0] += this[1, 0] * vY
        this[3, 1] += this[1, 1] * vY
        this[3, 2] += this[1, 2] * vY
        this[3, 3] += this[1, 3] * vY
        return this
    }

    fun Mat4f.translateZAssign(vX: Float): Mat4f {
        this[3, 0] += this[2, 0] * vX
        this[3, 1] += this[2, 1] * vX
        this[3, 2] += this[2, 2] * vX
        this[3, 3] += this[2, 3] * vX
        return this
    }
}
