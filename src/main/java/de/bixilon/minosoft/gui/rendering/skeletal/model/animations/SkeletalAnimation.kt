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

package de.bixilon.minosoft.gui.rendering.skeletal.model.animations

import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel.Companion.fromBlockCoordinates
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animator.SkeletalAnimator
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animator.keyframes.KeyframeChannels
import de.bixilon.minosoft.gui.rendering.skeletal.model.outliner.SkeletalOutliner
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.EMPTY_INSTANCE
import java.util.*

data class SkeletalAnimation(
    val uuid: UUID,
    val name: String,
    val loop: AnimationLoops = AnimationLoops.LOOP,
    val override: Boolean = false,
    val length: Float,
    val animators: Map<UUID, SkeletalAnimator>,
) {

    fun get(channel: KeyframeChannels, animatorUUID: UUID, time: Float): Vec3? {
        val animator = animators[animatorUUID] ?: return null

        return animator.get(channel, tweakTime(time))
    }

    private fun tweakTime(time: Float): Float {
        when (loop) {
            AnimationLoops.LOOP -> return time % length
            AnimationLoops.ONCE -> {
                if (time > length) {
                    return 0.0f
                }
            }
            AnimationLoops.HOLD -> {
                if (time > length) {
                    return length
                }
            }
        }
        return time
    }

    fun calculateTransform(outliner: SkeletalOutliner, animationTime: Float): Mat4 {
        val transform = Mat4()

        val rotation = get(KeyframeChannels.ROTATION, outliner.uuid, animationTime)
        if (rotation != null && rotation != Vec3.EMPTY_INSTANCE) {
            transform.translateAssign(outliner.origin.fromBlockCoordinates())
            transform.rotateAssign(-rotation.x.rad, Vec3(1, 0, 0))
            transform.rotateAssign(-rotation.y.rad, Vec3(0, 1, 0))
            transform.rotateAssign(-rotation.z.rad, Vec3(0, 0, 1))
            transform.translateAssign(-outliner.origin.fromBlockCoordinates())
        }
        val scale = get(KeyframeChannels.SCALE, outliner.uuid, animationTime)
        if (scale != null && (scale.x != 1.0f || scale.y != 1.0f || scale.z != 1.0f)) {
            transform.scaleAssign(scale)
        }
        val position = get(KeyframeChannels.POSITION, outliner.uuid, animationTime)
        if (position != null && position != Vec3.EMPTY_INSTANCE) {
            transform[3, 0] += position.x
            transform[3, 1] += position.y
            transform[3, 2] += position.z
        }

        return transform
    }

    fun canClear(animationTime: Float): Boolean {
        if (loop == AnimationLoops.LOOP) {
            return false
        }
        if (animationTime < length) {
            return false
        }
        if (loop == AnimationLoops.ONCE) {
            return true
        }
        // ToDo: Check HOLD
        return false
    }
}
