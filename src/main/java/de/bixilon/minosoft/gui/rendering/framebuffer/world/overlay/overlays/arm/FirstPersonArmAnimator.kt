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

package de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.overlays.arm

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.data.entities.entities.player.Arms
import de.bixilon.minosoft.gui.rendering.entity.models.minecraft.player.PlayerModel
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.AnimationLoops
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.SkeletalAnimation
import de.bixilon.minosoft.gui.rendering.skeletal.model.animations.animator.keyframes.KeyframeChannels
import de.bixilon.minosoft.gui.rendering.skeletal.model.outliner.SkeletalOutliner

class FirstPersonArmAnimator(private val player: PlayerModel) : SkeletalAnimation {
    override val name: String = ""
    override val loop = AnimationLoops.LOOP
    override val length: Float = 100.0f

    // TODO (swinging: move arm to front, rotate)

    override fun get(channel: KeyframeChannels, outliner: SkeletalOutliner, time: Float): Vec3? {
        if (channel != KeyframeChannels.ROTATION) {
            return null
        }
        if (player.entity.mainArm == Arms.LEFT) {
            return Vec3(120, 20, 0)
        }
        return Vec3(120, -20, -10)
    }
}
