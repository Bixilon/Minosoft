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

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animator.SkeletalAnimator
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animator.keyframes.KeyframeChannels
import java.util.*

data class StaticSkeletalAnimation(
    val uuid: UUID,
    override val name: String,
    override val loop: AnimationLoops = AnimationLoops.LOOP,
    override val length: Float,
    val animators: Map<UUID, SkeletalAnimator>,
) : SkeletalAnimation {

    override fun get(channel: KeyframeChannels, animatorUUID: UUID, time: Float): Vec3? {
        val animator = animators[animatorUUID] ?: return null

        return animator.get(channel, tweakTime(time))
    }
}
