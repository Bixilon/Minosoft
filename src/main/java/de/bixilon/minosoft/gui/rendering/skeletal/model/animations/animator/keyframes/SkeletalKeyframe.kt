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

package de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animator.keyframes

import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.interpolateLinear
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.interpolateSine
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.toVec3
import glm_.vec3.Vec3
import java.util.*

data class SkeletalKeyframe(
    val channel: KeyframeChannels,
    val dataPoints: List<Map<String, Any>>,
    val uuid: UUID,
    val time: Float,
    val interpolation: KeyframeInterpolations = KeyframeInterpolations.LINEAR,
) {
    fun interpolateDataWith(other: List<Map<String, Any>>, delta: Float): Vec3 {
        val value1 = dataPoints[0].toVec3()
        val value2 = other[0].toVec3()

        return when (interpolation) {
            KeyframeInterpolations.LINEAR -> interpolateLinear(delta, value1, value2)
            KeyframeInterpolations.SINE -> interpolateSine(delta, value1, value2)
            // ToDo
            else -> interpolateSine(delta, value1, value2)
        }
    }
}

