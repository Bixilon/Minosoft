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

package de.bixilon.minosoft.gui.rendering.entities.model.human

import de.bixilon.minosoft.gui.rendering.entities.feature.SkeletalFeature
import de.bixilon.minosoft.gui.rendering.entities.model.human.animator.ArmAnimator
import de.bixilon.minosoft.gui.rendering.entities.model.human.animator.HeadPosition
import de.bixilon.minosoft.gui.rendering.entities.model.human.animator.LegAnimator
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.entities.util.EntitySpeed
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel

abstract class HumanModel<R : EntityRenderer<*>>(renderer: R, model: BakedSkeletalModel) : SkeletalFeature(renderer, model) {
    val head = instance.transform.children["head"]?.let { HeadPosition(this, it) }
    val leg = LegAnimator(this, instance.transform.children["left_leg"]!!, instance.transform.children["right_leg"]!!)
    val arm = ArmAnimator(this, instance.transform.children["left_arm"]!!, instance.transform.children["right_arm"]!!)

    val speed = EntitySpeed(renderer.entity)

    override fun updatePosition() {
        super.updatePosition()
        head?.update()
    }

    override fun update(millis: Long, delta: Float) {
        speed.update(delta)
        super.update(millis, delta)
        leg.update(delta)
        arm.update(delta)
    }
}
