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

import de.bixilon.kotlinglm.GLM
import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3

object Mat4Util {
    private val empty = Mat4()

    val Mat4.Companion.EMPTY_INSTANCE get() = empty

    fun Mat4.rotateDegreesAssign(rotation: Vec3): Mat4 {
        if (rotation.x != 0.0f) rotateX(this, rotation.x.rad)
        if (rotation.y != 0.0f) rotateY(this, rotation.y.rad)
        if (rotation.z != 0.0f) rotateZ(this, rotation.z.rad)
        return this
    }

    fun Mat4.rotateRadAssign(rotation: Vec3): Mat4 {
        if (rotation.x != 0.0f) rotateX(this, rotation.x)
        if (rotation.y != 0.0f) rotateY(this, rotation.y)
        if (rotation.z != 0.0f) rotateZ(this, rotation.z)
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
        System.arraycopy(empty.array, 0, array, 0, Mat4.length)
    }

    fun Mat4.rotateXAssign(rad: Float): Mat4 {
        rotateX(this, rad)

        return this
    }

    fun rotateX(m: Mat4, angle: Float) {
        val c = GLM.cos(angle)
        val s = GLM.sin(angle)


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

    fun rotateY(m: Mat4, angle: Float) {
        val c = GLM.cos(angle)
        val s = GLM.sin(angle)


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

    fun rotateZ(m: Mat4, angle: Float) {
        val c = GLM.cos(angle)
        val s = GLM.sin(angle)

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

    fun Mat4.translateXAssign(vX: Float): Mat4 {
        this[3, 0] += this[0, 0] * vX
        this[3, 1] += this[0, 1] * vX
        this[3, 2] += this[0, 2] * vX
        this[3, 3] += this[0, 3] * vX
        return this
    }
    fun Mat4.translateYAssign(vY: Float): Mat4 {
        this[3, 0] += this[1, 0] * vY
        this[3, 1] += this[1, 1] * vY
        this[3, 2] += this[1, 2] * vY
        this[3, 3] += this[1, 3] * vY
        return this
    }

    fun Mat4.translateZAssign(vX: Float): Mat4 {
        this[3, 0] += this[2, 0] * vX
        this[3, 1] += this[2, 1] * vX
        this[3, 2] += this[2, 2] * vX
        this[3, 3] += this[2, 3] * vX
        return this
    }
}
