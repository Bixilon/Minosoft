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

import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.math.MathConstants.PIf
import de.bixilon.kutil.math.interpolation.FloatInterpolation.interpolateLinear
import de.bixilon.minosoft.gui.rendering.entities.model.human.HumanModel
import de.bixilon.minosoft.gui.rendering.skeletal.instance.TransformInstance
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.rotateDegreesAssign
import kotlin.math.sin

class ArmAnimator(
    val model: HumanModel<*>,
    val left: TransformInstance,
    val right: TransformInstance,
) {
    private var maxAngle = 0.0f
    private var progress = 0.0f
    private var strength = 1.0f

    private fun updateAngle(delta: Float) {
        this.maxAngle = interpolateLinear(model.speed.value * VELOCITY_ANGLE, 0.0f, MAX_ANGLE)
        this.progress += strength * delta * 5.0f * model.speed.value
        this.progress %= 2.0f
    }

    fun update(delta: Float) {
        updateAngle(delta)
        apply()
    }

    private fun apply() {
        if (this.maxAngle == 0.0f) return
        val progress = sin((progress - 1.0f) * PIf) * this.maxAngle

        left.value
            .translateAssign(left.pivot)
            .rotateDegreesAssign(Vec3(progress, 0.0f, 0.0f))
            .translateAssign(left.nPivot)
        right.value
            .translateAssign(left.pivot)
            .rotateDegreesAssign(Vec3(-progress, 0.0f, 0.0f))
            .translateAssign(left.nPivot)
    }

    private companion object {
        const val MAX_ANGLE = 45.0f
        const val VELOCITY_ANGLE = 5.0f
    }
}
