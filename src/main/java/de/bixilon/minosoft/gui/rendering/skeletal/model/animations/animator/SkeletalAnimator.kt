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

package de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animator

import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animator.keyframes.SkeletalKeyframe
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.toVec3
import glm_.vec3.Vec3
import kotlin.math.PI
import kotlin.math.sin

data class SkeletalAnimator(
    val name: String,
    val type: String, // ToDo: enum
    val keyframes: List<SkeletalKeyframe>,
) {

    fun getRotation(time: Float): Vec3 {
        var rotation = Vec3.EMPTY

        var firstKeyframe: SkeletalKeyframe? = null
        var secondKeyframe: SkeletalKeyframe? = null

        for (keyframe in keyframes) {
            if (firstKeyframe == null) {
                firstKeyframe = keyframe
                continue
            }
            if (time <= keyframe.time) {
                if (secondKeyframe != null) {
                    firstKeyframe = secondKeyframe
                }
                secondKeyframe = keyframe
                continue
            }
            break
        }

        if (firstKeyframe == null || secondKeyframe == null) {
            return rotation
        }

        val firstRotation = firstKeyframe.dataPoints[0].toVec3()
        val secondRotation = secondKeyframe.dataPoints[0].toVec3()

        val delta = (time - firstKeyframe.time) / (secondKeyframe.time - firstKeyframe.time)
        rotation = firstRotation + (secondRotation - firstRotation) * (sin(delta * PI / 2))


        return rotation
    }
}
