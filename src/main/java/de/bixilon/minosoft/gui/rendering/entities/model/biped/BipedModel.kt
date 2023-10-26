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

package de.bixilon.minosoft.gui.rendering.entities.model.biped

import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.minosoft.gui.rendering.entities.feature.SkeletalFeature
import de.bixilon.minosoft.gui.rendering.entities.renderer.EntityRenderer
import de.bixilon.minosoft.gui.rendering.skeletal.baked.BakedSkeletalModel
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.rotateRadAssign

abstract class BipedModel(renderer: EntityRenderer<*>, model: BakedSkeletalModel) : SkeletalFeature(renderer, model) {
    val head = instance.transform.children["head"]!!


    override fun updatePosition() {
        super.updatePosition()
        val info = renderer.info

        val pitch = info.rotation.pitch
        head.value
            .translateAssign(head.pivot)
            .rotateRadAssign(Vec3(-pitch.rad, 0.0f, 0.0f))
            .translateAssign(head.nPivot)
    }
}
