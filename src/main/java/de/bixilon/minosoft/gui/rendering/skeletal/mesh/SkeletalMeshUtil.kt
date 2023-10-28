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

package de.bixilon.minosoft.gui.rendering.skeletal.mesh

import de.bixilon.kotlinglm.vec3.Vec3

object SkeletalMeshUtil {
    private fun encodePart(part: Float): Int {
        val unsigned = (part + 1.0f) / 2.0f // remove negative sign
        return (unsigned * 15.0f).toInt() and 0x0F
    }

    fun encodeNormal(normal: Vec3): Int {
        val x = encodePart(normal.x)
        val y = encodePart(normal.y)
        val z = encodePart(normal.z)

        return (y shl 8) or (z shl 4) or (x)
    }
}
