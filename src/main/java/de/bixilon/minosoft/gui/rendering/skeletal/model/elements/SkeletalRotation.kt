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

package de.bixilon.minosoft.gui.rendering.skeletal.model.elements

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.models.block.element.ModelElement.Companion.BLOCK_SIZE
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY

data class SkeletalRotation(
    val value: Vec3,
    val origin: Vec3? = null,
    val rescale: Boolean = false,
) {

    fun apply(offset: Vec3, from: Vec3, to: Vec3): SkeletalRotation {
        var origin = this.origin
        if (origin == null) {
            origin = ((to + from) / 2.0f / BLOCK_SIZE) + offset
        }

        return SkeletalRotation(value, origin, rescale)
    }

    companion object {
        val EMPTY = SkeletalRotation(Vec3.EMPTY, Vec3.EMPTY, false)
    }
}
