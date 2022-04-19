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

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animator.keyframes.KeyframeChannels
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animator.keyframes.SkeletalKeyframe

data class SkeletalAnimator(
    val name: String,
    val type: String, // ToDo: enum
    val keyframes: List<SkeletalKeyframe>,
) {

    fun get(channel: KeyframeChannels, time: Float): Vec3? {
        var firstKeyframe: SkeletalKeyframe? = null
        var secondKeyframe: SkeletalKeyframe? = null

        for (keyframe in keyframes) {
            if (keyframe.channel != channel) {
                continue
            }
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
            return null
        }
        val delta = (time - firstKeyframe.time) / (secondKeyframe.time - firstKeyframe.time)
        return firstKeyframe.interpolateDataWith(secondKeyframe.dataPoints, delta)
    }

}
