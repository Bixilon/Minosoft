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

package de.bixilon.minosoft.gui.rendering.camera.shaking

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.swizzle.xz
import de.bixilon.kutil.avg.FloatAverage
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.config.profile.profiles.rendering.camera.shaking.ShakingC
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import kotlin.math.sin

class CameraShaking(
    private val camera: Camera,
    private val profile: ShakingC,
) {
    private var cameraTransform: Mat4? = Mat4()
    private var damaged: Boolean = false
    private val speed = FloatAverage(5 * ProtocolDefinition.TICK_TIME * 1_000_000L, 0.0f)
    private val physics = camera.context.connection.camera.entity.physics


    fun update(): Boolean {
        if (this.damaged) {
            if (profile.damage) {
                this.cameraTransform = updateDamage()
                this.damaged = false
                return true
            }
        }
        if (profile.walking) {
            this.cameraTransform = updateBobbing()
            return true

        }
        return false
    }

    private fun updateBobbing(): Mat4? {
        if (!this.physics.onGround) {
            this.speed += 0.0f
            return null
        }
        val transform = Mat4()
        val time = millis()
        this.speed += this.physics.velocity.xz.length2().toFloat() // velocity affects how quick it goes
        val speed = minOf(this.speed.avg, 0.25f)
        val translation = bobbing(time, speed, TRANSLATION_FREQUENCY * profile.speed, TRANSLATION_STRENGTH * profile.intensity)
        val rotation = bobbing(time, speed, ROTATION_FREQUENCY * profile.speed, ROTATION_STRENGTH * profile.intensity)
        transform.translateAssign(Vec3(0, translation, 0))
        transform.rotateAssign(rotation, Vec3(0, 0, 1))
        return transform
    }

    private fun bobbing(time: Long, speed: Float, frequency: Float, intensity: Float): Float {
        val seconds = time / 1000.0

        val sin = sin(seconds * frequency).toFloat()
        return (sin * speed * intensity) / MINIMUM_SPEED
    }

    private fun updateDamage(): Mat4 {
        val transform = Mat4()
        val time = millis()
        val rotation = bobbing(time, 0.5f, DAMAGE_FREQUENCY * profile.speed, DAMAGE_STRENGTH * profile.intensity)
        transform.rotateAssign(rotation, Vec3(0, 0, 1))
        return transform
    }

    fun onDamage() {
        this.damaged = true
    }

    fun transform(): Mat4? {
        return cameraTransform
    }

    companion object {
        private const val DAMAGE_FREQUENCY = 40.0f
        private const val DAMAGE_STRENGTH = 0.004f
        private const val ROTATION_STRENGTH = 0.002f
        private const val ROTATION_FREQUENCY = 10.0f
        private const val TRANSLATION_STRENGTH = 0.1f
        private const val TRANSLATION_FREQUENCY = 15.0f
        private const val MINIMUM_SPEED = 0.1f
    }
}
