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

package de.bixilon.minosoft.gui.rendering.entities.model.animal

import de.bixilon.minosoft.gui.rendering.entities.feature.SkeletalFeature
import de.bixilon.minosoft.gui.rendering.entities.model.animator.HeadAnimator
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.entities.renderer.living.animal.AnimalRenderer
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel

class AnimalModel<R : EntityRenderer<*>>(renderer: R, model: BakedSkeletalModel) : SkeletalFeature(renderer, model) {
    val head = instance.transform["head"]?.let { HeadAnimator(renderer, it) }

    override fun updatePosition() {
        super.updatePosition()
        head?.update()
    }

    override fun updateInstance() {
        super.updateInstance()
        if (renderer is AnimalRenderer<*>) {
            instance.matrix.scaleAssign(renderer.scale)
        }
    }
}
