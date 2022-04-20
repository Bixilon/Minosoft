/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
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

object Mat4Util {

    fun Mat4.rotateDegreesAssign(rotation: Vec3): Mat4 {
        if (rotation.x != 0.0f) {
            rotateAssign(rotation.x.rad, Vec3(1, 0, 0))
        }
        if (rotation.y != 0.0f) {
            rotateAssign(rotation.y.rad, Vec3(0, 1, 0))
        }
        if (rotation.z != 0.0f) {
            rotateAssign(rotation.z.rad, Vec3(0, 0, 1))
        }
        return this
    }
}
