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

package de.bixilon.minosoft.gui.rendering.models.unbaked.element

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.FloatUtil.toFloat
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.toVec3

data class UnbakedElementRotation(
    val origin: Vec3,
    val axis: Axes,
    val angle: Float,
    val rescale: Boolean,
) {
    companion object {

        operator fun invoke(data: Map<String, Any>): UnbakedElementRotation {
            return UnbakedElementRotation(
                origin = data["origin"]?.toVec3()?.apply { this /= UnbakedElement.BLOCK_RESOLUTION } ?: Vec3(0.5f), // default: center
                axis = Axes[data["axis"].toString()],
                angle = data["angle"].toFloat(),
                rescale = data["rescale"]?.toBoolean() ?: false,
            )
        }
    }
}
