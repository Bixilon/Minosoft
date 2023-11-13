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

package de.bixilon.minosoft.gui.rendering.entities.model.human.animator

import de.bixilon.kotlinglm.func.rad
import de.bixilon.minosoft.gui.rendering.entities.model.human.HumanModel
import de.bixilon.minosoft.gui.rendering.skeletal.instance.TransformInstance
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.rotateXAssign

class LegAnimator(
    val model: HumanModel<*>,
    val left: TransformInstance,
    val right: TransformInstance,
) {

    fun update(delta: Float) {
        apply()
    }

    private fun apply() {
        val angle = model.speedAnimator.getAngle(MAX_ANGLE).rad

        left.value
            .translateAssign(left.pivot)
            .rotateXAssign(-angle)
            .translateAssign(left.nPivot)
        right.value
            .translateAssign(left.pivot)
            .rotateXAssign(angle)
            .translateAssign(left.nPivot)
    }

    private companion object {
        const val MAX_ANGLE = 45.0f
    }
}
