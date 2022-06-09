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

package de.bixilon.minosoft.gui.rendering.entity.models.minecraft.player

import de.bixilon.kotlinglm.func.deg
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.AnimationLoops
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animator.keyframes.KeyframeChannels
import de.bixilon.minosoft.gui.rendering.skeletal.model.outliner.SkeletalOutliner
import kotlin.math.PI
import kotlin.math.cos

class LegAnimator(
    model: PlayerModel,
) : ExtremitiesAnimator(model) {
    override val name: String = "leg_animator"
    override val loop: AnimationLoops = AnimationLoops.LOOP
    override val length: Float = 2.0f

    override fun get(channel: KeyframeChannels, outliner: SkeletalOutliner, time: Float): Vec3? {
        if (channel != KeyframeChannels.ROTATION) {
            return null
        }
        if (outliner.name == "LEFT_LEG") {
            return calculateAngle(time)
        }
        if (outliner.name == "RIGHT_LEG") {
            return calculateAngle(time - 1.0f)
        }
        return null
    }

    private fun calculateAngle(time: Float): Vec3 {
        val angle = cos(time * PI) * getVelocityMultiplier()
        return Vec3(angle.deg, 0, 0)
    }
}
