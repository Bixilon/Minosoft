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

import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animator.SkeletalAnimator
import glm_.vec3.Vec3
import java.util.*

data class SkeletalAnimation(
    val uuid: UUID,
    val name: String,
    val loop: AnimationLoops = AnimationLoops.LOOP,
    val override: Boolean = false,
    val length: Float,
    val animators: Map<UUID, SkeletalAnimator>,
) {
    fun getRotation(time: Float): Vec3 {
        val animator = animators.values.iterator().next()

        var time = time
        when (loop) {
            AnimationLoops.LOOP -> time % length
            AnimationLoops.ONCE -> {
                if (time > length) {
                    time = 0.0f
                }
            }
            AnimationLoops.HOLD -> {
                if (time > length) {
                    time = length
                }
            }
        }

        return animator.getRotation(time)
    }
}
