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

package de.bixilon.minosoft.gui.rendering.models.raw.block.element

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.json.JsonObject
import de.bixilon.kutil.primitive.BooleanUtil.toBoolean
import de.bixilon.kutil.primitive.FloatUtil.toFloat
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.gui.rendering.models.raw.block.element.ModelElement.Companion.BLOCK_SIZE
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.toVec3

class ElementRotation(
    val origin: Vec3,
    val axis: Axes,
    val angle: Float,
    val rescale: Boolean = false,
) {
    companion object {
        private val ORIGIN = Vec3(0.5f)

        fun deserialize(data: JsonObject): ElementRotation? {
            val angle = data["angle"]?.toFloat() ?: 0.0f
            val rescale = data["rescale"]?.toBoolean() ?: false

            if (angle == 0.0f && !rescale) return null
            val origin = data["origin"]?.toVec3()?.apply { this /= BLOCK_SIZE } ?: ORIGIN
            val axis = Axes[data["axis"].toString()]

            return ElementRotation(origin, axis, angle, rescale)
        }
    }
}
