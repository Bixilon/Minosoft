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
    private var rotation = 0.0f
    private val speed = FloatAverage(5 * ProtocolDefinition.TICK_TIME * 1_000_000L, 0.0f)
    private val physics = camera.context.connection.camera.entity.physics

    val isEmpty: Boolean get() = rotation == 0.0f

    fun update(): Boolean {
        var bobbing = 0.0f
        if (profile.walking) {
            bobbing = updateBobbing()
        }
        var damage = 0.0f
        if (profile.damage) {
            damage = updateDamage()
        }

        val rotation = bobbing + damage

        if (rotation == this.rotation) return false
        this.rotation = rotation
        return true
    }

    private fun updateBobbing(): Float {
        if (!this.physics.onGround) {
            this.speed += 0.0f
            return 0.0f
        }
        val time = millis()
        this.speed += this.physics.velocity.xz.length2().toFloat() // velocity affects how quick it goes
        val speed = minOf(this.speed.avg, 0.25f)
        return bobbing(time, speed, FREQUENCY * profile.speed, STRENGTH * profile.intensity)
    }

    private fun bobbing(time: Long, speed: Float, frequency: Float, intensity: Float): Float {
        val seconds = time / 1000.0

        val sin = sin(seconds * frequency).toFloat()
        return (sin * speed * intensity) / MINIMUM_SPEED
    }

    private fun updateDamage(): Float {
        //TODO
        return 0.0f
    }

    fun onDamage() {
        //TODO
    }

    fun transform(): Mat4? {
        if (this.rotation == 0.0f) return null
        return Mat4()
            .rotateAssign(this.rotation, ROTATION)
    }

    companion object {
        private val ROTATION = Vec3(0, 0, 1)
        private const val STRENGTH = 0.002f
        private const val FREQUENCY = 10.0f
        private const val MINIMUM_SPEED = 0.1f
    }
}
