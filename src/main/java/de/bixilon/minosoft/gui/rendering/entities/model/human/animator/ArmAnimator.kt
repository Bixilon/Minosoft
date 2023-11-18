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
import de.bixilon.kotlinglm.func.sin
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kutil.math.MathConstants.PIf
import de.bixilon.minosoft.data.entities.entities.player.Arms
import de.bixilon.minosoft.gui.rendering.entities.model.human.HumanModel
import de.bixilon.minosoft.gui.rendering.skeletal.instance.TransformInstance
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.rotateRadAssign
import de.bixilon.minosoft.gui.rendering.util.mat.mat4.Mat4Util.rotateXAssign

class ArmAnimator(
    val model: HumanModel<*>,
    val left: TransformInstance,
    val right: TransformInstance,
) {
    private var swinging = FloatArray(Arms.VALUES.size) { Float.NaN }

    fun update(delta: Float) {
        apply()
        for ((arm, progress) in swinging.withIndex()) {
            if (progress.isNaN()) continue
            swinging[arm] += delta * 5.0f
            if (swinging[arm] >= 1.0f) {
                swinging[arm] = Float.NaN
            }
        }
    }

    private fun apply() {
        val angle = model.speedAnimator.getAngle(MAX_ANGLE).rad

        apply(Arms.LEFT, angle)
        apply(Arms.RIGHT, -angle)
    }

    private fun apply(arm: Arms, walking: Float) {
        val transform = this[arm]
        val swinging = swinging[arm.ordinal]
        transform.value
            .translateAssign(right.pivot)

        if (swinging.isNaN()) {
            transform.value.rotateXAssign(walking)
        } else {
            var swing = 1.0f - (swinging)
            swing *= swing
            swing *= swing
            swing = 1.0f - swing

            val sin = (swing * PIf).sin

            val z = (1.0f - (swing * 1.2f)) * (-20.0f).rad // TODO: animate the 1.2f back to 1.0f
            val y = sin * 0.2f
            val x = sin * -1.4f


            transform.value.rotateRadAssign(Vec3(x, y, if (arm == Arms.RIGHT) z else -z))
        }
        transform.value
            .translateAssign(right.nPivot)

    }

    private operator fun get(arm: Arms) = when (arm) {
        Arms.LEFT -> left
        Arms.RIGHT -> right
    }


    fun swing(arm: Arms) {
        if (!swinging[arm.ordinal].isNaN()) return
        swinging[arm.ordinal] = 0.0f
    }

    private companion object {
        const val MAX_ANGLE = 40.0f
    }
}
